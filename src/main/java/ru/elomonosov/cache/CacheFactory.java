package ru.elomonosov.cache;

import ru.elomonosov.level.CacheLevel;
import ru.elomonosov.level.CacheLevelFactory;
import ru.elomonosov.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class CacheFactory {

    class Levels {

        List<Level> levelList;
        List<Integer> sizeList;

        public Levels() {
            this.levelList = new ArrayList<>();
            this.sizeList = new ArrayList<>();
        }

        void addLevel(Level level, int size) {
            levelList.add(level);
            sizeList.add(size);
        }

        void clear() {
            levelList.clear();
            sizeList.clear();
        }

        Level getLevelParameter(int num) {
            return levelList.get(num);
        }

        int getSizeParameter(int num) {
            return sizeList.get(num);
        }
    }

    private static final CacheFactory INSTANCE = new CacheFactory();
    private Levels levels;
    private CacheStrategy cacheStrategy;

    private CacheFactory() {
        levels = new Levels();
    }

    public static CacheFactory getInstance() {
        return INSTANCE;
    }

    public void addLevel(Level level, int maxSize) throws CacheFactoryException {

        if (!(maxSize > 0)) {
            throw new IllegalArgumentException("Level size must be more than 0");
        }

        if (level == null) {
            throw new IllegalArgumentException("Level must be not null");
        }

        levels.addLevel(level, maxSize);
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

        if (cacheStrategy == null) {
            cacheStrategy = CacheStrategy.LEAST_RECENTLY_USED;
        }

        int levelsQuantity = levels.levelList.size();
        List<CacheLevel> cacheLevelList;
        if (levelsQuantity == 0) {
            cacheLevelList = new ArrayList<>(2);
            cacheLevelList.add(CacheLevelFactory.INSTANCE.getCacheLevel(cacheStrategy, Level.MEMORY, baseSize, 0));
            cacheLevelList.add(CacheLevelFactory.INSTANCE.getCacheLevel(cacheStrategy, Level.FILE, (int) (baseSize * multiplier), 1));
        } else {
            cacheLevelList = new ArrayList<>(levelsQuantity);
            for (int i = 0; i < levelsQuantity; i++) {
                cacheLevelList.add(CacheLevelFactory.INSTANCE.getCacheLevel(cacheStrategy, levels.getLevelParameter(i), levels.getSizeParameter(i), i));
            }
        }

        return new Cache(cacheStrategy, cacheLevelList);
    }

    public void clearLevels() {
        levels.clear();
    }
}
