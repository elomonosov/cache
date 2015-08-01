package ru.elomonosov.cache;

public class CacheException extends Exception {

    public CacheException(String info, Exception cause) {
        super(info, cause);
    }
}
