package ru.elomonosov.cache;

import ru.elomonosov.level.*;

import java.util.ArrayList;
import java.util.List;

public class CacheFactory {

    private static final CacheFactory INSTANCE = new CacheFactory();

    private List<CacheLevel> cacheLevelList = new ArrayList<>();

    private CacheStrategy cacheStrategy;

    private CacheFactory() {

    }

    public static CacheFactory getInstance() {
        return INSTANCE;
    }

    public void addLevel(Level level, int size) throws CacheFactoryException {
        if (!(size > 0)) {
            throw new IllegalArgumentException("Level size must be more than 0");
        }
        CacheLevel cacheLevel = null;
        try {
            switch (level) {
                case MEMORY: {
                    cacheLevel = new InMemoryCache(size);
                    break;
                }
                case FILE: {
                    cacheLevel = new InFileCache(size);
                    break;
                }
            }
        } catch (CacheLevelException e) {
            throw new CacheFactoryException("Cannot add level.", e);
        }
        cacheLevelList.add(cacheLevel);
    }

    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    public Cache getCache() throws CacheFactoryException {
        return getCache(10);
    }

    public Cache getCache(int baseSize) throws CacheFactoryException {
       return getCache(baseSize, 10);
    }

    public Cache getCache(int baseSize, double multiplier) throws CacheFactoryException {
        if (!(baseSize > 0)) {
            throw new IllegalArgumentException("Base size must be more than 0");
        }
        if (!(multiplier > 0)) {
            throw new IllegalArgumentException("Multiplier must be more than 0");
        }

        Cache result;
        if (cacheStrategy == null) {
            cacheStrategy = CacheStrategy.LEAST_RECENTLY_USED;
        }
        if (cacheLevelList.size() == 0) {
            addLevel(Level.MEMORY, baseSize);
            int size = (int) (baseSize * multiplier);

            // Check for size overflow
            if (size > 0) {
                addLevel(Level.FILE, size);
            } else {
                addLevel(Level.FILE, Integer.MAX_VALUE);
            }
        }
        result = new Cache(cacheStrategy, cacheLevelList);
        cacheLevelList = new ArrayList<>();
        cacheStrategy = null;
        return result;
    }
}
