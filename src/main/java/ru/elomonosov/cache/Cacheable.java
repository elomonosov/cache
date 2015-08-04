package ru.elomonosov.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public interface Cacheable extends Serializable {
    long getId();

    default int getCacheLength() throws CacheException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        try {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);) {
                objectOutputStream.writeObject(this);
            }
        } catch (IOException e) {
            throw new CacheException("Cannot get cached data length.", e);
        }
        return byteOutputStream.toByteArray().length;
    }
}
