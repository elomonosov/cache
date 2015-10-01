package ru.elomonosov.level;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.elomonosov.cache.CacheStrategy;
import ru.elomonosov.cache.TestCacheData;
import ru.elomonosov.test.Result;
import ru.elomonosov.util.ClassNameUtil;

import java.util.*;

import static org.junit.Assert.*;

public class CacheLevelTest {

    private static final Logger logger = LoggerFactory.getLogger(ClassNameUtil.getCurrentClassName());

    private static final Map<CacheLevel, Long> cacheLevelMap = new LinkedHashMap<>(); // keep metrics for cache level in the value

    @BeforeClass
    public static void prepare() throws Exception {
        // add CacheLevel implementation to cacheLevelMap to test it
        cacheLevelMap.put(new InMemoryLevel(CacheStrategy.LEAST_RECENTLY_USED, 500, 0), 0L);
        cacheLevelMap.put(new InFileLevel(CacheStrategy.LEAST_RECENTLY_USED, 500, 1), 0L);
    }

    @AfterClass
    public static void clear() throws Exception {
        Iterator<Map.Entry<CacheLevel, Long>> iterator = cacheLevelMap.entrySet().iterator();
        long testTime = 0;
        List<Result> results = new ArrayList<>(cacheLevelMap.size());
        while (iterator.hasNext()) {
            StringBuilder sb = new StringBuilder();
            Map.Entry<CacheLevel, Long> entry = iterator.next();
            sb.append("Level ");
            sb.append(entry.getKey().getOrder());
            sb.append(" ");
            sb.append(entry.getKey().getClass().getSimpleName());
            sb.append(" time in test: ");
            sb.append( (entry.getValue() / 1_000_000_000d) );
            sb.append(" seconds ---- ");
            results.add(new Result(sb.toString(), entry.getValue()));
            testTime += entry.getValue();
        }
        double onePercent = testTime / 100d;


        StringBuilder allResult = new StringBuilder("Cache level test \n");
        for (Result result : results) {
            result.setRelativeTime((result.getAbsoluteTime() / onePercent));
            result.setText(result.getText() + result.getRelativeTime() + " %");
            allResult.append(result.getText());
            allResult.append("\n");
        }
        allResult.append("Time taken: ");
        allResult.append(testTime / 1_000_000_000d);
        allResult.append(" seconds.");

        logger.info(allResult.toString());
        System.out.println(allResult);
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
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheLevelMap.get(cacheLevel);
            cacheLevelMap.put(cacheLevel, oldResult + resultTime);
        }
    }

    @After
    public void tearDown() throws Exception {
        Iterator<CacheLevel> iterator = cacheLevelMap.keySet().iterator();
        while(iterator.hasNext()) {
            CacheLevel cacheLevel = iterator.next();
            long timeBefore = System.nanoTime();
                cacheLevel.clear();
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheLevelMap.get(cacheLevel);
            cacheLevelMap.put(cacheLevel, oldResult + resultTime);
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
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheLevelMap.get(cacheLevel);
            cacheLevelMap.put(cacheLevel, oldResult + resultTime);
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
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheLevelMap.get(cacheLevel);
            cacheLevelMap.put(cacheLevel, oldResult + resultTime);
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
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheLevelMap.get(cacheLevel);
            cacheLevelMap.put(cacheLevel, oldResult + resultTime);
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
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheLevelMap.get(cacheLevel);
            cacheLevelMap.put(cacheLevel, oldResult + resultTime);
        }
    }
}