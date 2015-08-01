package ru.elomonosov.level;

public class CacheLevelException extends Exception {
    public CacheLevelException(String info, Exception cause) {
        super(info, cause);
    }
}
