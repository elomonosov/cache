package ru.elomonosov.cache;

import org.junit.*;
import ru.elomonosov.level.CacheLevelException;
import ru.elomonosov.level.Level;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CacheTest {

    private static final int LEVEL_MAX_SIZE = 5;
    private static List<Cache> cacheList = new ArrayList<>();
    private static List<Integer> cacheMaxSizeList = new ArrayList<>();

    @BeforeClass
    public static void prepare() throws Exception {
        CacheFactory cacheFactory = CacheFactory.getInstance();

        // Add caches to cacheList to test it
        cacheFactory.addLevel(Level.MEMORY, LEVEL_MAX_SIZE);
        cacheFactory.addLevel(Level.FILE, LEVEL_MAX_SIZE);
        cacheFactory.setCacheStrategy(CacheStrategy.LEAST_RECENTLY_USED);
        Cache cache = cacheFactory.getCache();
        cacheList.add(cache);
        cacheMaxSizeList.add(cache.maxSize());
    }

    @AfterClass
    public static void clear() throws CacheException {
        for (Cache cache : cacheList) {
            cache.delete();
        }
    }

    @Before
    public void setUp() throws CacheException {
        int cacheNum = 0;
        for (Cache cache : cacheList) {
            for (int i = 0; i < (cacheMaxSizeList.get(cacheNum)) - 1; i++) {
                cache.put(new TestCacheData(i, "testCashData" + i));
            }
            cacheNum++;
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
        int cacheNum = 0;
        for (Cache cache : cacheList) {
            int cacheMaxSize = cacheMaxSizeList.get(cacheNum);
            cache.put(new TestCacheData(cacheMaxSize - 1, "testCashData" + (cacheMaxSize - 1)));
            int cacheSize = cache.size();
            assertEquals("Unique item was pasted. Cache must be full and contains " + (cacheMaxSize) + ", but contains " + cacheSize + ". \n" + cache.toString(), true, cache.isFull());
        }
    }

    @Test
    public void testPutNotUnique() throws CacheException, CacheLevelException {
        for (Cache cache : cacheList) {
            int cacheSize = cache.size();
            cache.put(new TestCacheData(0, "testCashData" + 0));
            int newCacheSize = cache.size();
            assertEquals("Not unique item was pasted. Cache must be " + cacheSize + ", but is " + newCacheSize + ". \n" + cache.toString(), cacheSize, newCacheSize);
        }
    }

    @Test
    public void testGet() throws CacheException {
        TestCacheData storedData = new TestCacheData(0, "testCashData0");

        for (Cache cache : cacheList) {
            TestCacheData cashedData = (TestCacheData) cache.get(0);
            assertEquals("Cannot get correct item.", storedData, cashedData);
        }
    }

    @Test
    public void testLRU() throws CacheException {
        for (Cache cache : cacheList) {
            if (cache.strategy() == CacheStrategy.LEAST_RECENTLY_USED) {
                cache.put(new TestCacheData(-1, "newData"));

            }
        }
    }

    private List<List<Cacheable>> getLRUTestData(int levelCount, int levelsSize) {
        int num = 0;
        List<List<Cacheable>> referenceCache = new ArrayList<>(levelCount);
        for (int i = 0; i < levelCount; i++) {
            List<Cacheable> referenceLevel = new ArrayList<>(levelsSize);
            for (int k = 0; k < levelsSize; k++) {
                if ((i == 0) && (k == 0)) {
                    referenceLevel.add(-1, new TestCacheData(-1, "newData"));
                } else {
                    referenceLevel.add(new TestCacheData(num + 1, "testCashData" + num + 1));
                }
            }
            referenceCache.add(referenceLevel);
        }
        referenceCache.get(levelCount - 1).remove(levelsSize - 1);

        return referenceCache;
    }
}