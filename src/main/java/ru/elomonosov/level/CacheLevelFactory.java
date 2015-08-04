package ru.elomonosov.level;

import ru.elomonosov.cache.CacheStrategy;

public class CacheLevelFactory {

    public static final CacheLevelFactory INSTANCE = new CacheLevelFactory();

    private CacheLevelFactory() {

    }

    public CacheLevel getCacheLevel(CacheStrategy cacheStrategy, Level level, int maxSize, int order) throws CacheLevelFactoryException {
        CacheLevel result = null;
        try {
            switch (level) {
                case MEMORY: {
                    result = new InMemoryCache(cacheStrategy, maxSize, order);
                    break;
                }
                case FILE: {
                    result = new InFileCache(cacheStrategy, maxSize, order);
                    break;
                }
                case SEPARATE_FILES:{
                    result = new InFileSepCache(cacheStrategy, maxSize, order);
                    break;
                }
            }
        } catch (CacheLevelException e) {
            throw new CacheLevelFactoryException("Cannot create level " + level + " order " + order, e);
        }
        return result;
    }
}
