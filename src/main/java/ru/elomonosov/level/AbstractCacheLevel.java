package ru.elomonosov.level;


/**
 * Level of the cache must keep info about it order in the cache. Cache order determine the behavior of the cachche level when item is displaced.
 */
public abstract class AbstractCacheLevel implements CacheLevel {

    protected final int order;

    public AbstractCacheLevel(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return  order;
    }
}
