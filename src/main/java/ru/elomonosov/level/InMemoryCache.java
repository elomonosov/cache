package ru.elomonosov.level;

import ru.elomonosov.cache.CacheStrategy;
import ru.elomonosov.cache.Cacheable;


public class InMemoryCache extends AbstractCacheLevel {

    public InMemoryCache(int maxSize) {
        super(maxSize);
    }

    @Override
    public void put(Cacheable cacheable) throws CacheLevelException {
        putItem(cacheable);
    }

    @Override
    public Cacheable get(long id) throws CacheLevelException {
        return getItem(id);
    }

    @Override
    public Cacheable get(CacheStrategy cacheStrategy) {
        return getItem(cacheStrategy);
    }

    @Override
    public boolean remove(long id) throws CacheLevelException {
        return removeItem(id);
    }

    @Override
    public Cacheable pull(CacheStrategy cacheStrategy) {
        return pullItem(cacheStrategy);
    }

    @Override
    public void clear() {
        cachedData.clear();
    }

    @Override
    public void delete() {
        cachedData.clear();
    }
}
