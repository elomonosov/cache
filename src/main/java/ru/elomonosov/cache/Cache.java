package ru.elomonosov.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.elomonosov.level.*;
import ru.elomonosov.util.ClassNameUtil;

import java.util.List;

public final class Cache {

    private static final Logger logger = LoggerFactory.getLogger(ClassNameUtil.getCurrentClassName());

    private final CacheStrategy cacheStrategy;
    private final List<CacheLevel> cacheLevelList;

    /**
     * Constructs an empty cache with the specified displacement algorithm and levels.
     *
     * @param cacheStrategy displacement algorithm.
     * @param cacheLevelList list of cache levels, sorted from first to last.
     */
    protected Cache(CacheStrategy cacheStrategy, List<CacheLevel> cacheLevelList){
        this.cacheStrategy = cacheStrategy;
        this.cacheLevelList = cacheLevelList;
    }

    public List<CacheLevel> getCacheLevelList() {
        return cacheLevelList;
    }

    public void put(Cacheable cachable) throws CacheException {
        logger.info("Putting item [id {}] in the cache...", cachable.getId());
        logger.info("Trying to find and remove item with the same id");
        removeItem(cachable.getId());
        displace(cachable, 0);
    }

    /**
     * Get item in the cache by id.
     * Last time used time for this item will be updated.
     * @param id item id.
     * @return null if there is no item with the specified id in the cache.
     * @throws CacheException
     */
    public Cacheable get(long id) throws CacheException {
        int levelNum = 0;
        logger.info("Searching item [id {}]", id);
        for (CacheLevel cacheLevel : cacheLevelList) {
            Cacheable cacheable;
            try {
                cacheable = cacheLevel.pull(id);
                if (cacheable != null) {
                    if (levelNum != 0) {
                        logger.info("Item was found, putting it on the top level.");
                        displace(cacheable, 0);
                        return cacheable;
                    } else {
                        logger.info("Item was found on top level, putting it here.");
                        put(cacheable);
                        return cacheable;
                    }
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
     * Internal method, used to save item on the level.
     * If level is full, shift appropriate item on the next level. If it is the last level, appropriate item will be removed.
     * Appropriate item will be determined by the cache strategy.
     * @param cachable item that need to be saved on a level.
     * @param level level to save the item.
     * @throws CacheException if item cannot be saved.
     */
    private void displace(Cacheable cachable, int level) throws CacheException {
        try {
            CacheLevel cacheLevel = cacheLevelList.get(level);
            if (!cacheLevel.remove(cachable.getId())) { // if there was no item with the same id in the level
                if (cacheLevel.isFull()) {
                    if (level == cacheLevelList.size() - 1) {            // if current level is last, removeItem the appropriate item and break recursive call
                        cacheLevel.remove(cacheStrategy);
                    } else {
                        Cacheable displacedData = cacheLevel.pull(cacheStrategy); // if current level is not last, move appropriate item to next cache level
                        logger.info("Displace item [id {}] on the next level.", displacedData.getId());
                        displace(displacedData, ++level);
                    }
                    cacheLevel.put(cachable);   // save item on current level //
                } else {
                    cacheLevel.put(cachable); // The simpliest case. Put cached object on this level - it is not full because item was removed.
                }
            }
        } catch (CacheLevelException e ) {
            throw new CacheException("Cannot displace item with id = " + cachable.getId(), e);
        }
    }

    /**
     * Find item in the cache by id.
     * Last time used time for this item will not be updated.
     * @param id
     * @return null if there is no item with the specified id in the cache.
     * @throws CacheException
     *
     */
    public Cacheable find(long id) throws CacheException {
        logger.info("Getting item [id {}] from cache.", id);
        Cacheable result = null;
        try {
            int levelNum = 1;
            for (CacheLevel cacheLevel : cacheLevelList) {
                result = cacheLevel.get(id);
                if (result != null) {
                    logger.info("Item has been found on level {}.", levelNum);
                    break;
                }
                levelNum++;
            }
        } catch (CacheLevelException e) {
            throw new CacheException("Cannot get item with id " + id, e);
        }
        return result; // return null now if nothing was found in loop body
    }

    private int removeItem(long id) throws CacheException {
        int result = -1;
        logger.info("Removing the item [id {}].", id);
        try {
            for(int i = 0; i < cacheLevelList.size(); i++)
                if (cacheLevelList.get(i).remove(id)) {
                    result = i;
                    logger.info("Item [id {}] has been removed", id);
                    break;
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
                result += cacheLevel.maxSize();
            }
        } catch (CacheLevelException e) {
            throw new CacheException("Cache max size was not counted.", e);
        }
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

    public void delete() throws CacheException{
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
            result = cacheLevelList.get(cacheLevelList.size()-1).isFull();
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
            header2 = "Cache contains " + size() + " items of " +maxSize() +".\n";
            int levelNum = 1;
            for (CacheLevel cacheLevel : getCacheLevelList()){
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
