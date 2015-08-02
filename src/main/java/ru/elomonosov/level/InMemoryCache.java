package ru.elomonosov.level;

import ru.elomonosov.cache.CacheStrategy;
import ru.elomonosov.cache.Cacheable;


public class InMemoryCache extends AbstractCacheLevel implements CacheLevel {

    public InMemoryCache(CacheStrategy cacheStrategy, int maxSize) {
        super(cacheStrategy, maxSize);
    }

    @Override
    public void put(Cacheable cacheable) throws CacheLevelException {
        cacheData.put(cacheable);
    }

    @Override
    public Cacheable get(long id) throws CacheLevelException {
        return cacheData.get(id);
    }

    @Override
    public Cacheable getByStrategy() throws CacheLevelException {
        return cacheData.getByStrategy();
    }

    @Override
    public Cacheable pull(long id) throws CacheLevelException {
        return cacheData.pull(id);
    }

    @Override
    public Cacheable pullByStrategy() throws CacheLevelException {
        return cacheData.pullByStrategy();
    }

    @Override
    public void clear() throws CacheLevelException {
        cacheData.clear();
    }

    @Override
    public void delete() throws CacheLevelException {
        cacheData.clear();
    }

}
