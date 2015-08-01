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
import java.util.TreeMap;


public class InFileCache extends AbstractCacheLevel {

    private static final Logger logger = LoggerFactory.getLogger(ClassNameUtil.getCurrentClassName());

    protected final Path filePath;

    public InFileCache(int maxSize, Path filePath) throws InFileLevelException {
        super(maxSize);
        try {
            if (!Files.exists(filePath)) {
                Files.createDirectory(filePath);
            }
            this.filePath = Files.createTempFile(Paths.get(filePath.toString()), "cache", ".tmp");
            logger.info("File {} was created.", filePath);
        } catch (IOException e) {
            throw new InFileLevelException("Cannot create file " + filePath, e);
        }
    }

    public InFileCache(int maxSize) throws InFileLevelException {
        this(maxSize, Paths.get(System.getProperty("user.dir"), "tmp"));
    }

    @Override
    public void put(Cacheable cacheable) throws CacheLevelException {
        loadCachedData();
        putItem(cacheable);
        saveCachedData();
    }

    @Override
    public Cacheable get(long id) throws CacheLevelException {
        loadCachedData();
        Cacheable result = getItem(id);
        saveCachedData();
        return result;
    }

    @Override
    public Cacheable get(CacheStrategy cacheStrategy) throws CacheLevelException {
        loadCachedData();
        Cacheable result = getItem(cacheStrategy);
        saveCachedData();
        return result;
    }

    @Override
    public boolean remove(long id) throws CacheLevelException {
        loadCachedData();
        boolean result = removeItem(id);
        saveCachedData();
        return result;
    }

    @Override
    public Cacheable pull(CacheStrategy cacheStrategy) throws InFileLevelException {
        loadCachedData();
        logger.info("Pull item? Search condition is: {}", cacheStrategy);
        Cacheable result = pullItem(cacheStrategy);
        saveCachedData();
        return result;
    }

    @Override
    public int size() throws InFileLevelException {
        loadCachedData();
        return cachedData.size();
    }

    @Override
    public boolean isFull() throws InFileLevelException {
        loadCachedData();
        boolean result = isItemsFull();
        return result;
    }

    @Override
    public void clear() throws InFileLevelException {
        loadCachedData();
        cachedData.clear();
        saveCachedData();
    }

    @Override
    public void delete() throws InFileLevelException {
        try {
            Files.delete(filePath);
            logger.info("File {} was deleted.", filePath);
        } catch (IOException e) {
            throw new InFileLevelException("Cannot delete file + " + filePath, e);
        }
    }

    private void loadCachedData() throws InFileLevelException {
        try {
            try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
                cachedData = (TreeMap<Long, Cacheable>) in.readObject();
                logger.info("Cache loaded from file, size is {}", cachedData.size());
            }
        } catch (NullPointerException | EOFException e) {
            logger.info("Cache loading failed, file is empty.");
            //cachedData.clear(); // TODO think of
        } catch (IOException | ClassNotFoundException e) {
            throw new InFileLevelException("Cannot read cached objects from file " + filePath, e);
        }
    }

    private void saveCachedData() throws InFileLevelException {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
                out.writeObject(cachedData);
                logger.info("Cache saved to file, size is {}", cachedData.size());
            } catch (IOException e) {
            throw new InFileLevelException("Cannot write cached object to file " + filePath, e);
        }
        System.gc(); // TODO other solution?
    }
}
