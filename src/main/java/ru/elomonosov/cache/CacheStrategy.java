package ru.elomonosov.cache;

/**
 * Cache strategies that determines what cached object will be discarded first when maximum cache size is reached.
 */
public enum CacheStrategy {
    /**
     * Least recently used item will be discarded first.
     */
    LEAST_RECENTLY_USED,

    /**
     * Most recently used item will be discarded first.
     */
    MOST_RECENTLY_USED
}
