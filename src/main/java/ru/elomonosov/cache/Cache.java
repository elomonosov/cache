package ru.elomonosov.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.elomonosov.level.CacheLevel;
import ru.elomonosov.level.CacheLevelException;
import ru.elomonosov.util.ClassNameUtil;

import java.util.List;

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

    public void put(Cacheable cacheable) throws CacheException {
        logger.info("Putting item [id {}] in the cache...", cacheable.getId());
        logger.info("Check for item with the same id...");
        removeItem(cacheable.getId()); // remove item with the same id from the cache
        //displace(cacheable, 0);
        putByStrategy(cacheable, 0);
        logger.info("Item [id{}] was added.", cacheable.getId());
    }

    private void putByStrategy(Cacheable cacheable, int levelNum) throws CacheException {
        try {
            CacheLevel cacheLevel = cacheLevelList.get(levelNum);
            logger.info("level {}:", levelNum);
            if (cacheLevel.isFull()) {                                  // if level is full, strategy defined item must be displaced by the one that need to be saved
                Cacheable displacedData = cacheLevel.pullByStrategy();
                if (levelNum < levelCount()) {                     // if it is not the last level, shift displaced data to next level
                    putByStrategy(displacedData, ++levelNum);
                    cacheLevel.put(cacheable);
                }
            } else {
                cacheLevel.put(cacheable); // The simplest case. Put cached object on this level - it is not full
            }
        } catch (CacheLevelException e) {
            throw new CacheException("Cannot put item with id = " + cacheable.getId() + " in level " + levelNum, e);
        }
    }

    /**
     * Get item in the cache by id.
     * Last time used time for this item will be updated.
     *
     * @param id item id.
     * @return null if there is no item with the specified id in the cache.
     * @throws CacheException
     */
    public Cacheable get(long id) throws CacheException {
        int levelNum = 0;
        logger.info("Searching item [id {}]", id);
        for (CacheLevel cacheLevel : cacheLevelList) {
            logger.info("level {}:", levelNum);
            Cacheable cacheable;
            try {
                cacheable = cacheLevel.get(id);
                if (cacheable != null) {
                    logger.info("Item was found, putting it on the top level.");
                    putByStrategy(cacheable, 0);
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

    public int levelCount() {
        return cacheLevelList.size();
    }

    public int levelSize(int levelNum) throws CacheException {
        int result;
        try {
            result = cacheLevelList.get(levelNum).size();
        } catch (CacheLevelException e) {
            throw new CacheException("Cannot get size of  level " + levelNum, e);
        }
        return result;
    }

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

    public CacheStrategy strategy() {
        return cacheStrategy;
    }

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

    public void delete() throws CacheException {
        logger.info("Deleting the cache.");
        logger.info(toString());
        try {
            for (CacheLevel cacheLevel : cacheLevelList) {
                cacheLevel.delete();
            }
        } catch (CacheLevelException e) {
            throw new CacheException("Cannot delete cache.", e);
        }
    }

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
