package ru.elomonosov.level;

public class InFileLevelException extends CacheLevelException {

    public InFileLevelException(String info, Exception cause) {
        super(info, cause);
    }
}
