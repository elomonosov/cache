package ru.elomonosov.level;

import ru.elomonosov.cache.CacheStrategy;

public class CacheLevelFactory {

    public static final CacheLevelFactory INSTANCE = new CacheLevelFactory();

    private CacheLevelFactory() {

    }

    public CacheLevel getCacheLevel(CacheStrategy cacheStrategy, Level level, int maxSize) throws CacheLevelFactoryException {
        CacheLevel result = null;
        try {
            switch (level) {
                case MEMORY: {
                    result = new InMemoryCache(cacheStrategy, maxSize);
                    break;
                }
                case FILE: {
                    result = new InFileCache(cacheStrategy, maxSize);
                    break;
                }
            }
        } catch (CacheLevelException e) {
            throw new CacheLevelFactoryException("Cannot create level", e);
        }
        return result;
    }
}
