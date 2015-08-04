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
import java.util.LinkedHashMap;


public class InFileCache extends AbstractCacheLevel implements CacheLevel{

    private static final Logger logger = LoggerFactory.getLogger(ClassNameUtil.getCurrentClassName());

    protected final Path filePath;

    public InFileCache(CacheStrategy cacheStrategy, int maxSize, int order, Path dirPath) throws InFileLevelException {
        super(cacheStrategy, maxSize, order);
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            }
            this.filePath = Files.createTempFile(Paths.get(dirPath.toString()), "cache", ".tmp");
            logger.info("File {} was created.", dirPath);
        } catch (IOException e) {
            throw new InFileLevelException("Cannot create file " + dirPath, e);
        }
    }

    public InFileCache(CacheStrategy cacheStrategy, int maxSize, int order) throws InFileLevelException {
        this(cacheStrategy, maxSize, order, Paths.get(System.getProperty("user.dir"), "tmp"));
    }

    @Override
    public void put(Cacheable cacheable) throws CacheLevelException {
        loadCachedData();
        cacheData.put(cacheable);
        saveCachedData();
    }

    @Override
    public Cacheable get(long id) throws CacheLevelException {
        loadCachedData();
        Cacheable result = cacheData.get(id);
        saveCachedData();
        return result;
    }

    @Override
    public Cacheable getByStrategy() throws CacheLevelException {
        loadCachedData();
        Cacheable result = cacheData.getByStrategy();
        saveCachedData();
        return result;
    }

    @Override
    public Cacheable pull(long id) throws CacheLevelException {
        loadCachedData();
        Cacheable result = cacheData.pull(id);
        saveCachedData();
        return result;
    }

    @Override
    public Cacheable pullByStrategy() throws InFileLevelException {
        loadCachedData();
        logger.info("Pull item? Search condition is: {}", cacheData.cacheStrategy);
        Cacheable result = cacheData.pullByStrategy();
        saveCachedData();
        return result;
    }

    @Override
    public int size() throws InFileLevelException {
        loadCachedData();
        return cacheData.size();
    }

    @Override
    public boolean isFull() throws InFileLevelException {
        loadCachedData();
        return cacheData.isFull();
    }

    @Override
    public void clear() throws InFileLevelException {
        loadCachedData();
        cacheData.clear();
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new InFileLevelException("", e);
        }
    }

    private void loadCachedData() throws InFileLevelException {
        initIfNecessary();
        try {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
                cacheData.cacheMap = (LinkedHashMap<Long, Cacheable>) in.readObject();
                logger.info("Cache loaded from file, size is {}", cacheData.size());
            }
        } catch (NullPointerException | EOFException e) {
            logger.info("Cache loading failed, file is empty.");
            //cachedData.clear(); // TODO think of
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new InFileLevelException("Cannot read cached objects from file " + filePath, e);
        }
    }

    private void saveCachedData() throws InFileLevelException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            out.writeObject(cacheData.cacheMap);
            logger.info("Cache saved to file, size is {}", cacheData.size());
        } catch (IOException e) {
            throw new InFileLevelException("Cannot write cached object to file " + filePath, e);
        }
        System.gc(); // TODO other solution?
    }

    private void initIfNecessary() throws InFileLevelException {
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new InFileLevelException("Cannot init file", e);
            }
        }
    }
}
