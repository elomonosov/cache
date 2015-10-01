package ru.elomonosov.cache;

import ru.elomonosov.level.CacheLevel;
import ru.elomonosov.level.CacheLevelFactory;
import ru.elomonosov.level.Level;

import java.util.*;

public final class CacheFactory {


    private static final CacheFactory INSTANCE = new CacheFactory();

    public static CacheFactory getInstance() {
        return INSTANCE;
    }

    public Cache getCache() throws CacheFactoryException {
        return getCache(10);
    }

    public Cache getCache(int baseSize) throws CacheFactoryException {
        return getCache(baseSize, CacheStrategy.RANDOM);
    }

    public Cache getCache(int baseSize, CacheStrategy cacheStrategy) throws CacheFactoryException {
        return getCache(baseSize, cacheStrategy, 10);
    }

    public Cache getCache(int baseSize, CacheStrategy cacheStrategy, int multiplier) throws CacheFactoryException {
        List<Level> cacheLevels = new ArrayList<>(2);
        cacheLevels.add(Level.MEMORY);
        cacheLevels.add(Level.FILE);
        return getCache(baseSize, multiplier, cacheStrategy, cacheLevels);
    }

    public Cache getCache(CacheStrategy cacheStrategy, LinkedHashMap<Level, Integer> levelList) {
        if (cacheStrategy == null) {
            throw new IllegalArgumentException("Cache strategy must be not null");
        }
        if ((levelList == null)||(levelList.isEmpty())) {
            throw new IllegalArgumentException("Cache levels must be set");
        }
        Iterator<Map.Entry<Level, Integer>> iterator = levelList.entrySet().iterator();
        List<CacheLevel> cacheLevelList = new ArrayList<>(levelList.size());
        int i = 0;
        while(iterator.hasNext()) {
            Map.Entry<Level, Integer> entry = iterator.next();
            cacheLevelList.add(CacheLevelFactory.INSTANCE.getCacheLevel(cacheStrategy, entry.getKey(), entry.getValue(), i));
            i++;
        }
        return new Cache(cacheStrategy, cacheLevelList);
    }

    public Cache getCache(int baseSize, int multiplier, CacheStrategy cacheStrategy, List<Level> cacheLevels) throws CacheFactoryException {
        if (!(baseSize > 0)) {
            throw new IllegalArgumentException("Base size must be more than 0");
        }
        if (!(multiplier > 0)) {
            throw new IllegalArgumentException("Multiplier must be more than 0");
        }
        if (cacheStrategy == null) {
            throw new IllegalArgumentException("Cache strategy must be not null");
        }
        if ((cacheLevels == null)||(cacheLevels.isEmpty())) {
            throw new IllegalArgumentException("Cache levels must be set");
        }
        List<CacheLevel> cacheLevelList = new ArrayList<>(cacheLevels.size());
        for (int i = 0; i < cacheLevels.size(); i++) {
            cacheLevelList.add(CacheLevelFactory.INSTANCE.getCacheLevel(cacheStrategy, cacheLevels.get(i), (baseSize + (baseSize * multiplier)), i));
        }
        return new Cache(cacheStrategy, cacheLevelList);
    }
}
