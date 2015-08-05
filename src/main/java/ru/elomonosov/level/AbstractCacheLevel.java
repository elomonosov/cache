package ru.elomonosov.level;

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
