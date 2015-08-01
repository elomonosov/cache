package ru.elomonosov.level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.elomonosov.cache.CacheStrategy;
import ru.elomonosov.cache.Cacheable;
import ru.elomonosov.util.ClassNameUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class AbstractCacheLevel implements CacheLevel {

    private static final Logger logger = LoggerFactory.getLogger(ClassNameUtil.getCurrentClassName());

    /**
     * @field Contains time of last access to the cached object and cached object.
     *
     */
    protected SortedMap<Long, Cacheable> cachedData;

    /**
     * @field Maximum quantity of objects that can be stored.
     */
    protected int maxSize;

    protected AbstractCacheLevel(int maxSize) {
        this.maxSize = maxSize;
        cachedData = new TreeMap<>();
    }

    @Override
    public int maxSize() {
        logger.info("maxSize is {}", maxSize);
        return maxSize;
    }

    @Override
    public int size() throws CacheLevelException{
        logger.info("Size is {}", cachedData.size());
        return cachedData.size();
    }

    @Override
    public boolean remove(CacheStrategy cacheStrategy) throws CacheLevelException {
        logger.info("Remove item? Search condition is: {}", cacheStrategy);
        return removeItem(cacheStrategy);
    }

    @Override
    public Cacheable pull(long id) throws CacheLevelException {
        logger.info("Pull item with id = {}?", id);
        return pullItem(id);
    }

    @Override
    public boolean isFull()  throws CacheLevelException{
        logger.info("Is level full?");
        return isItemsFull();
    }

    @Override
    public boolean remove(Cacheable cacheable) throws CacheLevelException {
        logger.info("Remove item with id[{}]?", cacheable.getId());
        return remove(cacheable.getId());
    }

    public boolean isItemsFull() {
        logger.info("The level is {}", (cachedData.size() == maxSize()) ? "full." : "not full." );
        return (cachedData.size() == maxSize());
    }

    protected void putItem(Cacheable cacheable) throws CacheLevelException {
        logger.info("Putting item id[{}], trying to remove old item with the same id.", cacheable.getId());
        removeItem(cacheable.getId());
        cachedData.put(System.nanoTime(), cacheable); // update time of last access to the cached object.
        logger.info("Item saved, level size is {}", cachedData.size());
    }

    protected Cacheable getItem(long id) throws CacheLevelException {
        if (cachedData.isEmpty()) {
            logger.info("Level is empty, nothing found.");
            return null;
        } else {
            for (Map.Entry<Long, Cacheable> entry : cachedData.entrySet()) {
                Cacheable cacheable = entry.getValue();
                if (cacheable.getId() == id) {
                    logger.info("Item id[{}] was found.", id);
                    return cacheable;
                }
            }
            logger.info("Item id[{}] was not found,", id);
            return null; // return null now if didn't getItem anything in loop body
        }
    }



    protected Cacheable getItem(CacheStrategy cacheStrategy) {
        Cacheable result;
        if (cachedData.isEmpty()) {
            result = null;
            logger.info("Level is emty, nothing found.");
        } else{
            long resultKey = 0;
            switch (cacheStrategy) {
                case LEAST_RECENTLY_USED: {
                    resultKey = cachedData.lastKey();
                    break;
                }
                case MOST_RECENTLY_USED: {
                    resultKey = cachedData.firstKey();
                    break;
                }
            }
            result = cachedData.get(resultKey);
        }
        logger.info("Item id[{}] was found.", result.getId());
        return result;
    }

    protected boolean removeItem(long id) throws CacheLevelException {
        if (cachedData.isEmpty()) {
            logger.info("Level is empty, nothing removed.");
            return false;
        } else {
            Iterator<Map.Entry<Long, Cacheable>> iterator = cachedData.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<Long, Cacheable> entry = iterator.next();
                if (entry.getValue().getId() == id) {
                    iterator.remove();
                    logger.info("Item id[{}] removed, level size is {}.", cachedData.size());
                    return true;
                }
            }
        }
        logger.info("Item not found, nothing removed.");
        return false;
    }

    protected boolean removeItem(CacheStrategy cacheStrategy) throws CacheLevelException {
        if (cachedData.isEmpty()) {
            logger.info("Level is empty, nothing removed.");
            return false;
        } else{
            long resultKey = 0;
            switch (cacheStrategy) {
                case LEAST_RECENTLY_USED: {
                    resultKey = cachedData.firstKey();
                    break;
                }
                case MOST_RECENTLY_USED: {
                    resultKey = cachedData.lastKey();
                    break;
                }
            }
            cachedData.remove(resultKey);
            logger.info("Item id[{}] was removed, level size is {}.", resultKey, size());
            return true;
        }
    }

    protected Cacheable pullItem(long id) {
        if (cachedData.isEmpty()) {
            logger.info("The level is empty, nothing was found.");
            return null;
        } else {
            Iterator<Map.Entry<Long, Cacheable>> iterator = cachedData.entrySet().iterator();
            while (iterator.hasNext()) {
                Cacheable cacheable = iterator.next().getValue();
                if (cacheable.getId() == id) {
                    iterator.remove(); // remove item from the level because it's pulling, not just getting.
                    logger.info("Item id[{}] has been pulled.", id);
                    return cacheable;
                }
            }
        }
        logger.info("Level does not contain item [id {}].", id);
        return null; // return null if nothing found.
    }

    protected Cacheable pullItem(CacheStrategy cacheStrategy) {
        Cacheable result;
        if (cachedData.isEmpty()) {
            logger.info("The level is empty, nothing was found");
            result = null;
        } else {
            long resultKey = 0;
            switch (cacheStrategy) {
                case LEAST_RECENTLY_USED: {
                    resultKey = cachedData.firstKey();
                    break;
                }
                case MOST_RECENTLY_USED: {
                    resultKey = cachedData.lastKey();
                    break;
                }
            }
            result = cachedData.get(resultKey);
            cachedData.remove(resultKey);
            logger.info("Item [id {}] was pulled, level size is {} now.", result.getId(), cachedData.size());
        }
        return result;
    }
}
