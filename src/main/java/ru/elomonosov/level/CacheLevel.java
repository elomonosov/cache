package ru.elomonosov.level;

import ru.elomonosov.cache.Cacheable;

public interface CacheLevel {

    void put(Cacheable cacheable) throws CacheLevelException;

    Cacheable get(long id) throws CacheLevelException;

    Cacheable getByStrategy() throws CacheLevelException;

    Cacheable pull(long id) throws CacheLevelException;

    Cacheable pullByStrategy() throws CacheLevelException;

    int size() throws CacheLevelException;

    int maxSize() throws CacheLevelException;

    boolean isFull() throws CacheLevelException;

    void clear() throws CacheLevelException;

    void delete() throws CacheLevelException;
}
