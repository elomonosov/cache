package ru.elomonosov.level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.elomonosov.cache.CacheStrategy;
import ru.elomonosov.cache.Cacheable;
import ru.elomonosov.util.ClassNameUtil;

import java.util.*;

public abstract class AbstractCacheLevel implements CacheLevel {

    private static final Logger logger = LoggerFactory.getLogger(ClassNameUtil.getCurrentClassName());
    protected CacheData cacheData;

    public AbstractCacheLevel(CacheStrategy cacheStrategy, int maxSize) {
        this.cacheData = new CacheData(cacheStrategy, maxSize);
    }

    @Override
    public int maxSize() {
        logger.info("Level maxSize is {}", cacheData.maxSize);
        return cacheData.maxSize;
    }

    @Override
    public int size() throws CacheLevelException {
        logger.info("Size is {}", cacheData.size());
        return cacheData.size();
    }

    @Override
    public boolean isFull() throws CacheLevelException {
        logger.info("Level is {}.", cacheData.isFull() ? "full" : "not full");
        return cacheData.isFull();
    }

    protected class CacheData {

        final CacheStrategy cacheStrategy;
        final int maxSize;
        Map<Long, Cacheable> cacheMap;

        CacheData(CacheStrategy cacheStrategy, int maxSize) {
            this.maxSize = maxSize;
            this.cacheStrategy = cacheStrategy;

            switch (cacheStrategy) {
                case LEAST_RECENTLY_USED: {
                    cacheMap = new LinkedHashMap<>(maxSize, 1F, true);
                    break;
                }
                case RANDOM: {
                    cacheMap = new HashMap<>();
                    break;
                }
                default: {
                    cacheMap = null;
                    break;
                }
            }
        }

        boolean isFull() {
            return (cacheMap.size() == maxSize);
        }

        int size() {
            return cacheMap.size();
        }

        void clear() {
            cacheMap.clear();
        }

        void put(Cacheable cacheable) throws CacheLevelException {
            logger.info("Putting item id[{}].", cacheable.getId());
            cacheMap.put(cacheable.getId(), cacheable);
            logger.info("Item saved, level size is {}", cacheMap.size());
        }

        Cacheable get(long id) throws CacheLevelException {
            Cacheable result = cacheMap.get(id);
            logger.info("Item was {}found.", (result == null) ? "not " : "");
            return result;
        }

        Cacheable getByStrategy() {
            Cacheable result;
            if (isFull()) {
                result = null;
                logger.info("Level is empty, nothing was found.");
            } else {
                result = cacheMap.get(keyByStrategy());
            }
            return result;
        }

        long keyByStrategy() {
            long result = 0;
            switch (cacheStrategy) {
                case LEAST_RECENTLY_USED: {
                    Iterator<Long> iterator = cacheMap.keySet().iterator();
                    result = iterator.next();
                    break;
                }
                case RANDOM: {
                    int randomKeyNum = new Random().nextInt(cacheMap.size());
                    Iterator<Long> iterator = cacheMap.keySet().iterator();

                    for (int i = 0; i < randomKeyNum; i++) {
                        iterator.next();
                    }
                    result = iterator.next();
                    break;
                }
            }
            return result;
        }

        Cacheable removeByStrategy() {
            Cacheable result;
            if (isFull()) {
                logger.info("Level is empty, nothing removed.");
                result = null;
            } else {
                result = cacheMap.remove(keyByStrategy());
                if (result != null) {
                    logger.info("Item id[{}] removed, level size is {}.", result.getId(), cacheMap.size());
                } else {
                    logger.info("Item not found, nothing removed.");
                    result = null;
                }
            }
            return result;
        }

        boolean remove(long id) {
            boolean result;
            if (isFull()) {
                logger.info("Level is empty, nothing removed.");
                result = false;
            } else {
                if (cacheMap.remove(id) != null) {
                    logger.info("Item id[{}] removed, level size is {}.", id, cacheMap.size());
                    result = true;
                } else {
                    logger.info("Item not found, nothing removed.");
                    result = false;
                }
            }
            return result;
        }

        Cacheable pull(long id) {
            Cacheable result;
            if (cacheMap.isEmpty()) {
                logger.info("The level is empty, nothing was found.");
                return null;
            } else {
                result = cacheMap.remove(id);
                if (result != null) {
                    logger.info("Item id[{}] has been pulled.", id);
                } else {
                    logger.info("Level does not contain item [id {}].", id);
                }
            }
            return result;
        }

        Cacheable pullByStrategy() {
            Cacheable result;
            if (cacheMap.isEmpty()) {
                logger.info("The level is empty, nothing was found");
                result = null;
            } else {
                result = cacheMap.remove(keyByStrategy());
                logger.info("Item [id {}] was pulled, level size is {} now.", result.getId(), cacheMap.size());
            }
            return result;
        }
    }
}
