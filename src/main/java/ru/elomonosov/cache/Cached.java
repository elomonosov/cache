package ru.elomonosov.cache;

import java.io.Serializable;

public class Cached implements Serializable, Comparable<Cached>{

    private final long id;

    private long lastUsed;

    private final Cacheable cachedObject;

    public Cached(Cacheable cachedObject) {
        this.cachedObject = cachedObject;
        this.id = cachedObject.getId();
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void updateLastUsed() {
        this.lastUsed = System.nanoTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cached)) return false;

        Cached cached = (Cached) o;

        return id == cached.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public Cacheable getCachedObject() {
        return cachedObject;
    }


    @Override
    public int compareTo(Cached o) {
        return Long.compare(this.lastUsed, o.getLastUsed());
    }
}
