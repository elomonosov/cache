package ru.elomonosov.level;

public class InMemoryLevelException extends CacheLevelException {

    public InMemoryLevelException(String info, Exception cause) {
        super(info, cause);
    }
}
