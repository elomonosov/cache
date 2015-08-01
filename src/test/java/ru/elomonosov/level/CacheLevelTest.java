package ru.elomonosov.level;

import org.junit.*;
import ru.elomonosov.cache.CacheStrategy;
import ru.elomonosov.cache.TestCacheData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CacheLevelTest {

    //private static final int CACHED_DATA_COUNT = 3; // Cached items quantity that will be put on each cache level. Must be more than 1 and less than CACHE_MAX_SIZE
    private static final List<CacheLevel> cacheLevelList = new ArrayList<>();

    @BeforeClass
    public static void prepare() throws Exception {
        CacheLevelFactory factory = CacheLevelFactory.getInstance();
        cacheLevelList.add(factory.getCacheLevel(Level.MEMORY, 10));
        cacheLevelList.add(factory.getCacheLevel(Level.FILE, 10));
        // add CacheLevel implementation to test it
    }

    @AfterClass
    public static void clear() throws Exception {
        for (CacheLevel cacheLevel : cacheLevelList) {
            cacheLevel.delete();
        }
    }

    @Before
    public void setUp() throws Exception {
        for (CacheLevel cacheLevel : cacheLevelList) {
            for (int i = 0; i < cacheLevel.maxSize() - 1; i++) {
                cacheLevel.put(new TestCacheData(i, "testCacheData" + i));
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        for (CacheLevel cacheLevel : cacheLevelList) {
            cacheLevel.clear();
        }
    }

    @Test
    public void testPut() throws Exception {
        for (CacheLevel cacheLevel : cacheLevelList) {
            int sizeBeforeAdd = cacheLevel.size();
            cacheLevel.put(new TestCacheData(0, "testCacheData" + 0));
            assertEquals("Not all items are unique.", sizeBeforeAdd, cacheLevel.size());
        }
    }

    @Test
    public void testGet() throws Exception {
        int level = 0;
        for (CacheLevel cacheLevel : cacheLevelList) {
            TestCacheData testCacheData = new TestCacheData(5555, "storedData");
            cacheLevel.put(testCacheData);
            TestCacheData storedTestCacheData = (TestCacheData) cacheLevel.get(CacheStrategy.LEAST_RECENTLY_USED);
            assertEquals("Got wrong item, level " + level, testCacheData, storedTestCacheData);
            level++;
        }
    }

    @Test
    public void testRemove() throws Exception {
        for (CacheLevel cacheLevel : cacheLevelList) {
            int sizeBeforeRemove = cacheLevel.size();
            cacheLevel.remove(1);
            assertEquals("Item was not removed.", sizeBeforeRemove - 1, cacheLevel.size());
        }
    }

    @Test
    public void testIsFull() throws Exception {
        for (CacheLevel cacheLevel : cacheLevelList) {
            assertFalse("Cache is not full, but showed as full.", cacheLevel.isFull());

            for(int i = cacheLevel.size(); i < cacheLevel.maxSize(); i++) {
                cacheLevel.put(new TestCacheData(i, "testCacheData" + i));
            }
            assertTrue("Cache is full, but showed as not full.", cacheLevel.isFull());
        }
    }
}