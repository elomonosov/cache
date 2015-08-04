package ru.elomonosov.level;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.elomonosov.cache.CacheStrategy;
import ru.elomonosov.cache.TestCacheData;
import ru.elomonosov.util.ClassNameUtil;

import java.util.*;

import static org.junit.Assert.*;

public class CacheLevelTest {

    private static final Logger logger = LoggerFactory.getLogger(ClassNameUtil.getCurrentClassName());

    private static final Map<CacheLevel, Long> cacheLevelMap = new LinkedHashMap<>();

    @BeforeClass
    public static void prepare() throws Exception {
        // add CacheLevel implementation to cacheLevelMap to test it
        cacheLevelMap.put(new InMemoryCache(CacheStrategy.LEAST_RECENTLY_USED, 10, 0), 0L);
        cacheLevelMap.put(new InFileCache(CacheStrategy.LEAST_RECENTLY_USED, 10, 1), 0L);
        cacheLevelMap.put(new InFileSepCache(CacheStrategy.LEAST_RECENTLY_USED, 10, 2), 0L);
    }

    @AfterClass
    public static void clear() throws Exception {
        Iterator<Map.Entry<CacheLevel, Long>> iterator = cacheLevelMap.entrySet().iterator();
        List<String> stringResults = new ArrayList<>(cacheLevelMap.size());
        List<Long> timeResults = new ArrayList<>(cacheLevelMap.size());

        long testTime = 0;
        while(iterator.hasNext()) {
            StringBuilder sb = new StringBuilder();
            Map.Entry<CacheLevel, Long> entry = iterator.next();
            sb.append("Level ");
            sb.append(entry.getKey().getOrder());
            sb.append(" ");
            sb.append(entry.getKey().getClass().getCanonicalName());
            sb.append(" time in test: ");
            sb.append(entry.getValue());
            sb.append(" ---- ");
            stringResults.add(sb.toString());
            testTime += entry.getValue();
            timeResults.add(entry.getValue());
        }

        double onePercent = testTime / 100d;
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < cacheLevelMap.size(); i++) {
            result.append(stringResults.get(i));
            result.append(timeResults.get(i) / onePercent);
            result.append("%\n");
        }
        logger.info(result.toString());
        System.out.println(result);
    }

    @Before
    public void setUp() throws Exception {
        Iterator<CacheLevel> iterator = cacheLevelMap.keySet().iterator();
        while(iterator.hasNext()) {
            CacheLevel cacheLevel = iterator.next();
            long timeBefore = System.nanoTime();
            for (int i = 0; i < cacheLevel.maxSize() - 1; i++) {
                cacheLevel.put(new TestCacheData(i, "testCacheData" + i));
            }
            long timeAfter = System.nanoTime() - timeBefore;
            cacheLevelMap.put(cacheLevel, timeAfter);
        }
    }

    @After
    public void tearDown() throws Exception {
        Iterator<CacheLevel> iterator = cacheLevelMap.keySet().iterator();
        while(iterator.hasNext()) {
            CacheLevel cacheLevel = iterator.next();
            long timeBefore = System.nanoTime();
                cacheLevel.clear();
            long timeAfter = System.nanoTime() - timeBefore;
            //cacheLevelMap.put(cacheLevel, timeAfter);
        }
    }

    @Test
    public void testPut() throws Exception {
        Iterator<CacheLevel> iterator = cacheLevelMap.keySet().iterator();
        while(iterator.hasNext()) {
            CacheLevel cacheLevel = iterator.next();
            long timeBefore = System.nanoTime();
            int sizeBeforeAdd = cacheLevel.size();
            cacheLevel.put(new TestCacheData(0, "testCacheData" + 0));
            assertEquals("Not all items are unique.", sizeBeforeAdd, cacheLevel.size());
            long timeAfter = System.nanoTime() - timeBefore;
            cacheLevelMap.put(cacheLevel, timeAfter);
        }
    }

    @Test
    public void testGet() throws Exception {
        int level = 0;

        Iterator<CacheLevel> iterator = cacheLevelMap.keySet().iterator();
        while(iterator.hasNext()) {
            CacheLevel cacheLevel = iterator.next();
            long timeBefore = System.nanoTime();
            TestCacheData testCacheData = new TestCacheData(0, "testCacheData" + 0);
            TestCacheData storedTestCacheData = (TestCacheData) cacheLevel.getByStrategy();
            assertEquals("Got wrong item, level " + level, testCacheData, storedTestCacheData);
            level++;
            long timeAfter = System.nanoTime() - timeBefore;
            cacheLevelMap.put(cacheLevel, timeAfter);
        }
    }

    @Test
    public void testRemove() throws Exception {
        Iterator<CacheLevel> iterator = cacheLevelMap.keySet().iterator();
        while(iterator.hasNext()) {
            CacheLevel cacheLevel = iterator.next();
            long timeBefore = System.nanoTime();
            int sizeBeforeRemove = cacheLevel.size();
            cacheLevel.pull(1);
            assertEquals("Item was not removed.", sizeBeforeRemove - 1, cacheLevel.size());
            long timeAfter = System.nanoTime() - timeBefore;
            cacheLevelMap.put(cacheLevel, timeAfter);
        }
    }

    @Test
    public void testIsFull() throws Exception {
        Iterator<CacheLevel> iterator = cacheLevelMap.keySet().iterator();
        while(iterator.hasNext()) {
            CacheLevel cacheLevel = iterator.next();
            long timeBefore = System.nanoTime();
            assertFalse("Cache is not full, but showed as full.", cacheLevel.isFull());

            for (int i = cacheLevel.size(); i < cacheLevel.maxSize(); i++) {
                cacheLevel.put(new TestCacheData(i, "testCacheData" + i));
            }
            assertTrue("Cache is full, but showed as not full.", cacheLevel.isFull());
            long timeAfter = System.nanoTime() - timeBefore;
            cacheLevelMap.put(cacheLevel, timeAfter);
        }
    }
}