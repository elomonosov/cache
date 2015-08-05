package ru.elomonosov.level;

import ru.elomonosov.cache.CacheStrategy;

public final class CacheLevelFactory {

    public static final CacheLevelFactory INSTANCE = new CacheLevelFactory();

    private CacheLevelFactory() {

    }

    public CacheLevel getCacheLevel(CacheStrategy cacheStrategy, Level level, int maxSize, int order) {
        CacheLevel result = null;
        switch (level) {
            case MEMORY: {
                result = new InMemoryLevel(cacheStrategy, maxSize, order);
                break;
            }
            case FILE: {
                result = new InFileLevel(cacheStrategy, maxSize, order);
                break;
            }
        }
        return result;
    }
}
