package ru.elomonosov.level;

import ru.elomonosov.cache.CacheStrategy;
import ru.elomonosov.cache.Cacheable;

public interface CacheLevel {

    void put(Cacheable cacheable) throws CacheLevelException;

    Cacheable get(long id) throws CacheLevelException;

    Cacheable get(CacheStrategy cacheStrategy) throws CacheLevelException;

    boolean remove(long id) throws CacheLevelException;

    boolean remove(CacheStrategy cacheStrategy) throws CacheLevelException;

    boolean remove(Cacheable cacheable) throws CacheLevelException;

    Cacheable pull(long id) throws CacheLevelException;

    Cacheable pull(CacheStrategy cacheStrategy) throws CacheLevelException;

    int size() throws CacheLevelException;

    int maxSize() throws CacheLevelException;

    boolean isFull() throws CacheLevelException;

    void clear() throws CacheLevelException;

    void delete() throws CacheLevelException;
}
