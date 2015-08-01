package ru.elomonosov.level;

public class CacheLevelFactory {

    private static final CacheLevelFactory INSTANCE = new CacheLevelFactory();

    private CacheLevelFactory(){

    }

    public static CacheLevelFactory getInstance() {
        return INSTANCE;
    }

    public CacheLevel getCacheLevel(Level level, int maxSize) throws CacheLevelFactoryException {
        CacheLevel result = null;
        try {
            switch (level) {
                case MEMORY: {
                    result = new InMemoryCache(maxSize);
                    break;
                }
                case FILE: {
                    result = new InFileCache(maxSize);
                    break;
                }
            }
        } catch (CacheLevelException e) {
            throw new CacheLevelFactoryException("Cannot create level.", e);
        }
        return result;
    }
}
