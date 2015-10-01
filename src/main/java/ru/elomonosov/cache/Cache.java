package ru.elomonosov.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.elomonosov.level.CacheLevel;
import ru.elomonosov.level.CacheLevelException;
import ru.elomonosov.util.ClassNameUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Cache {

    private static final Logger logger = LoggerFactory.getLogger(ClassNameUtil.getCurrentClassName());

    private final CacheStrategy cacheStrategy;
    private final List<CacheLevel> cacheLevelList;

    /**
     * Constructs an empty cache with the specified displacement algorithm and levels.
     *
     * @param cacheStrategy  displacement algorithm.
     * @param cacheLevelList list of cache levels, sorted from first to last.
     */
    protected Cache(CacheStrategy cacheStrategy, List<CacheLevel> cacheLevelList) {
        this.cacheStrategy = cacheStrategy;
        this.cacheLevelList = cacheLevelList;
    }

    /**
     * Add item in the cache on the top level. Item with the same id will be removed from the cache.
     *
     * @param cacheable item that should be stored in the cache.
     * @throws CacheException if any level cannot reprocess item adding.
     */
    public synchronized void put(Cacheable cacheable) throws CacheException {
        if (cacheable != null) {
            logger.info("Putting item [id {}] in the cache...", cacheable.getId());
            logger.info("Check for item with the same id...");
            removeItem(cacheable.getId()); // remove item with the same id from the cache
            logger.info("Adding item to cache...");
            putByStrategy(cacheable, levelListByStrategy()); // put item in cache.
            logger.info("Item [id{}] was added.", cacheable.getId());
        }
    }

    /**
     * Internal method. Use strategy defined method of displace item from the cache levels if cache levels are full.
     *
     * @param cacheable item that should be stored in the cache.
     * @param levelsByStrategy
     * @throws CacheException if any level cannot reprocess item adding.
     */
    private void putByStrategy(Cacheable cacheable, List<CacheLevel> levelsByStrategy) throws CacheException {
        CacheLevel cacheLevel = null;
        try {
            cacheLevel = levelsByStrategy.get(0);
            if (cacheLevel != null) {
                if (cacheLevel.isFull()) {                                  // if level is full, strategy defined item must be displaced by the one that need to be saved
                    Cacheable displacedData = cacheLevel.pullByStrategy(); // get the item that need to be shifted or removed
                    if (levelsByStrategy.size() > 1) {                     // if it is not the last level, shift displaced data to next level
                        levelsByStrategy.remove(0);                        // remove level from the list of reprocessing levels
                        putByStrategy(displacedData, levelsByStrategy);    // call this method for the list of reprocessing levels
                        cacheLevel.put(cacheable);                         // add item to this level
                    }
                } else {
                    cacheLevel.put(cacheable); // The simplest case. Put cached object on this level - it is not full
                }
            } else {
                throw new CacheException("Cannot get level to put item on it", new NullPointerException());
            }
        } catch (CacheLevelException e) {
            throw new CacheException("Cannot put item with id = " + cacheable.getId() + " in level " + cacheLevel.getOrder(), e);
        }
    }

    /**
     * Internal method.
     * @return list of levels that are using in shifting displaced items from top levels to bottom.
     */
    private List<CacheLevel> levelListByStrategy() {
        List<CacheLevel> result = new ArrayList<>();
        switch (cacheStrategy) {
            case LEAST_RECENTLY_USED: {
                result.addAll(cacheLevelList);
                break;
            }
            case RANDOM: {
                CacheLevel randomCacheLevel = cacheLevelList.get(new Random().nextInt(cacheLevelList.size()));
                result.add(randomCacheLevel);
                break;
            }
        }
        return result;
    }

    /**
     * Get item in the cache by id.
     * Items order in the cache will be updated. Returned item will be put on the top level.
     *
     * @param id item id.
     * @return null if there is no item with the specified id in the cache.
     * @throws CacheException if any level cannot reprocess item getting.
     */
    public synchronized Cacheable get(long id) throws CacheException {
        int levelNum = 0;
        logger.info("Searching item [id {}]", id);
        for (CacheLevel cacheLevel : cacheLevelList) {
            logger.info("level {}:", levelNum);
            Cacheable cacheable;
            try {
                cacheable = cacheLevel.get(id);
                if (cacheable != null) {
                    logger.info("Item was found, putting it on the top level.");
                    putByStrategy(cacheable, levelListByStrategy());
                    return cacheable;
                }
            } catch (CacheLevelException | CacheException e) {
                throw new CacheException("Cannot get item with id = " + id, e);
            }
            levelNum++;
        }
        logger.info("Item has not found.");
        return null; // return null if nothing was found
    }

    /**
     * Internal method. Remove item from the cache.
     *
     * @param id item id
     * @return level order, that contained item that was removed. Returns -1 if item was not found.
     * @throws CacheException if any level cannot reprocess item removing.
     */
    private int removeItem(long id) throws CacheException {
        int result = -1;
        logger.info("Trying to remove the item [id {}].", id);
        try {
            for (int i = 0; i < cacheLevelList.size(); i++) {
                if (cacheLevelList.get(i).pull(id) != null) {
                    result = i;
                    logger.info("Item [id {}] has been removed", id);
                    break;
                }
            }
        } catch (CacheLevelException e) {
            throw new CacheException("Cannot remove item with id = " + id, e);
        }
        logger.info("Item {}{}", (result == -1) ? "has not found" : "has been removed from level ", (result == -1) ? "." : result);
        return result;
    }

    /**
     *
     * @return current size of cache, i.e. quantity of stored items.
     * @throws CacheException
     */
    public int size() throws CacheException {
        logger.info("Counting the cache size.");
        int result = 0;
        try {
            for (CacheLevel cacheLevel : cacheLevelList) { //size of the cache = sum of size of every level
                result += cacheLevel.size();
            }
        } catch (CacheLevelException e) {
            throw new CacheException("Cache size was not counted.", e);
        }
        return result;
    }

    /**
     *
     * @return quantity of items that can be stored in the cache.
     * @throws CacheException
     */
    public int maxSize() throws CacheException {
        logger.info("Counting the cache max size.");
        int result = 0;
        try {
            for (CacheLevel cacheLevel : cacheLevelList) { //max size of the cache = sum of max size of every level
                logger.info("Asking level...");
                result += cacheLevel.maxSize();
            }
        } catch (CacheLevelException e) {
            throw new CacheException("Cache max size was not counted.", e);
        }
        logger.info("Cache max size is {}.", result);
        return result;
    }

    /**
     * Shows what strategy is in use by the cache in performing all cache operations.
     * @return the cache strategy
     */
    public CacheStrategy strategy() {
        return cacheStrategy;
    }

    /**
     * Clears the cache from all items.
     * @throws CacheException if any level cannot be cleared.
     */
    public void clear() throws CacheException {
        logger.info("Clearing the cache.");
        logger.info(toString());
        try {
            for (CacheLevel cacheLevel : cacheLevelList) {
                cacheLevel.clear();
            }
        } catch (CacheLevelException e) {
            throw new CacheException("Cannot clear cache.", e);
        }
    }

    /**
     * Show if the cache is full or not.
     * @return true if the cache can store more items without displace already stored, and false in the other case.
     * @throws CacheException
     */
    public boolean isFull() throws CacheException {
        logger.info("Checking the cache for fill.");
        boolean result;
        try {
            result = cacheLevelList.get(cacheLevelList.size() - 1).isFull();
        } catch (CacheLevelException e) {
            throw new CacheException("Cannot check the cache for fill.", e);
        }
        return result;
    }

    @Override
    public String toString() {
        String header1;
        String header2;
        StringBuilder levelsInfo = new StringBuilder();
        String result;

        try {
            header1 = "Cache strategy: " + strategy() + "\n";
            header2 = "Cache contains " + size() + " items of " + maxSize() + ".\n";
            int levelNum = 1;
            for (CacheLevel cacheLevel : cacheLevelList) {
                levelsInfo.append(levelNum);
                levelsInfo.append(": ");
                levelsInfo.append(cacheLevel.size());
                levelsInfo.append(" items of ");
                levelsInfo.append(cacheLevel.maxSize());
                levelsInfo.append("\n");
                levelNum++;
            }
            result = header1 + header2 + levelsInfo;
        } catch (CacheLevelException | CacheException e) {
            result = "Cannot cast cache to string." + "\n" + e.getMessage();
        }
        return result;
    }
}
