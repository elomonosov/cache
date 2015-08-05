package ru.elomonosov.level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.elomonosov.cache.CacheStrategy;
import ru.elomonosov.cache.Cacheable;
import ru.elomonosov.util.ClassNameUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class InFileLevel implements CacheLevel {

    private static final Logger logger = LoggerFactory.getLogger(ClassNameUtil.getCurrentClassName());

    private Map<Long, Path> cachePaths;

    int maxSize;

    int order;

    CacheStrategy cacheStrategy;

    public InFileLevel(CacheStrategy cacheStrategy, int maxSize, int order) {
        this.maxSize = maxSize;
        this.order = order;
        this.cacheStrategy = cacheStrategy;
        switch (cacheStrategy) {
            case LEAST_RECENTLY_USED:
            {
                this.cachePaths = new LinkedHashMap<>();
                break;
            }
        }
    }

    @Override
    public void put(Cacheable cacheable) throws CacheLevelException {
        Path oldPath = cachePaths.get(cacheable.getId());
        if (oldPath != null) {
            // remove
        } else {
            try {
                Path path = Files.createTempFile(Paths.get(System.getProperty("user.dir"), "tmp"), "cache", ".tmp");
                try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(path.toFile())))) {
                    out.writeObject(cacheable);
                }
                cachePaths.put(cacheable.getId(), path);

            } catch (IOException e) {
                throw new CacheLevelException("", e);
            }
        }
    }

    @Override
    public Cacheable get(long id) throws CacheLevelException {
        Path path = cachePaths.get(id);
        Cacheable result;
        if (path == null) {
            result = null;
        } else {
            try {
                try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path.toFile())))) {
                    result = (Cacheable) in.readObject();
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new CacheLevelException("", e);
            }
        }
        return result;
    }

    @Override
    public Cacheable getByStrategy() throws CacheLevelException {
        Cacheable result;
        Long id = keyByStrategy();
        if (id == null) {
            result = null;
        } else {
            result = get(id);
        }
        return result;
    }

    @Override
    public Cacheable pull(long id) throws CacheLevelException {
        Cacheable result = get(id);
        if (result != null) {
            try {
                Files.delete(cachePaths.get(id));
                cachePaths.remove(id);
            } catch (IOException e) {
                throw new CacheLevelException("", e);
            }
        }
        return result;
    }

    @Override
    public Cacheable pullByStrategy() throws CacheLevelException {
        Cacheable result;
        Long id = keyByStrategy();
        if (id == null) {
            result = null;
        } else {
            result = pull(id);
        }
        return result;
    }

    @Override
    public int size() throws CacheLevelException {
        return cachePaths.size();
    }

    @Override
    public int maxSize() throws CacheLevelException {
        return maxSize;
    }

    @Override
    public boolean isFull() throws CacheLevelException {
        return cachePaths.size() == maxSize();
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void clear() throws CacheLevelException {
        if (!cachePaths.isEmpty()) {
            try {
                for(Path path : cachePaths.values() ) {
                    Files.delete(path);
                }
                cachePaths.clear();
            } catch (IOException e) {
                throw new CacheLevelException("", e);
            }
        }
    }

    private long keyByStrategy() {
        long result = 0;
        switch (cacheStrategy) {
            case LEAST_RECENTLY_USED: {
                Iterator<Long> iterator = cachePaths.keySet().iterator();
                result = iterator.next();
                break;
            }
            case RANDOM: {
                int randomKeyNum = new Random().nextInt(cachePaths.size());
                Iterator<Long> iterator = cachePaths.keySet().iterator();
                for (int i = 0; i < randomKeyNum; i++) {
                    iterator.next();
                }
                result = iterator.next();
                break;
            }
        }
        return result;
    }
}
