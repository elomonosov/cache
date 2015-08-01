package ru.elomonosov.cache;

import org.junit.*;
import ru.elomonosov.level.CacheLevelException;
import ru.elomonosov.level.Level;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CacheTest {

    private static List<Cache> cacheList = new ArrayList<>();

    @BeforeClass
    public static void prepare() throws Exception {
        CacheFactory cacheFactory = CacheFactory.getInstance();

        // Add caches to cacheList to test it
        cacheFactory.addLevel(Level.MEMORY, 10);
        cacheFactory.addLevel(Level.FILE, 10);
        cacheFactory.setCacheStrategy(CacheStrategy.LEAST_RECENTLY_USED);
        cacheList.add(cacheFactory.getCache());

        cacheFactory.addLevel(Level.MEMORY, 10);
        cacheFactory.addLevel(Level.FILE, 10);
        cacheFactory.setCacheStrategy(CacheStrategy.MOST_RECENTLY_USED);
        cacheList.add(cacheFactory.getCache());
    }

    @AfterClass
    public static void clear() throws CacheException {
        for (Cache cache : cacheList) {
            cache.delete();
        }
    }

    @Before
    public void setUp() throws CacheException {
        for (Cache cache : cacheList) {
            for (int i = 0; i < cache.maxSize() - 1; i++) {
                cache.put(new TestCacheData(i, "testCashData" + i));
            }
        }
    }

    @After
    public void tearDown() throws CacheException {
        for (Cache cache : cacheList) {
            cache.clear();
        }
    }

    @Test
    public void testPutUnique() throws CacheException, CacheLevelException {
        for (Cache cache : cacheList) {
            int cacheMaxSize = cache.maxSize();
            cache.put(new TestCacheData(cacheMaxSize - 1, "testCashData" + (cacheMaxSize - 1)));
            int cacheSize = cache.size();
            assertEquals("Unique item was pasted. Cache size must be full and contains " + (cacheMaxSize) + ", but contains " + cacheSize + ". \n" + cache.toString(), true, cache.isFull());
        }
    }

    @Test
    public void testPutNotUnique() throws CacheException, CacheLevelException {
        for (Cache cache : cacheList) {
            int cacheSize = cache.size();
            cache.put(new TestCacheData(0, "testCashData" + 0 ));
            int newCacheSize = cache.size();
            assertEquals("Not unique item was pasted. Cache size must be " + cacheSize + ", but is " + newCacheSize + ". \n" + cache.toString(), cacheSize, newCacheSize);
        }
    }

    @Test
    public void testGet() {
        //TODO implement
    }

    @Test
    public void testGetAndCompare() {
        //TODO implement
    }

    @Test
    public void testPutAndCompare() {
        //TODO implement
    }
}