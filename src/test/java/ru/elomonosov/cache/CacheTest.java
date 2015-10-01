package ru.elomonosov.cache;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.elomonosov.level.CacheLevelException;
import ru.elomonosov.level.Level;
import ru.elomonosov.test.Result;
import ru.elomonosov.util.ClassNameUtil;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class CacheTest {

    private static final Logger logger = LoggerFactory.getLogger(ClassNameUtil.getCurrentClassName());

    private static final int LEVEL_MAX_SIZE = 20;
    private static final Map<Cache, Long> cacheMap = new LinkedHashMap<>();

    @BeforeClass
    public static void prepare() throws Exception {
        CacheFactory cacheFactory = CacheFactory.getInstance();

        // Add caches to cacheMap to test it
        LinkedHashMap<Level, Integer> cacheLevels = new LinkedHashMap<>();
        cacheLevels.put(Level.MEMORY, LEVEL_MAX_SIZE);
        cacheLevels.put(Level.FILE, LEVEL_MAX_SIZE);
        Cache cache = cacheFactory.getCache(CacheStrategy.LEAST_RECENTLY_USED, cacheLevels);
        cacheMap.put(cache, 0L);
    }

    @AfterClass
    public static void clear() throws Exception {
        Iterator<Map.Entry<Cache, Long>> iterator = cacheMap.entrySet().iterator();
        long testTime = 0;
        List<Result> results = new ArrayList<>(cacheMap.size());
        int cacheNum = 0;
        while (iterator.hasNext()) {
            StringBuilder sb = new StringBuilder();
            Map.Entry<Cache, Long> entry = iterator.next();
            sb.append("Cache ");
            sb.append(cacheNum);
            sb.append(" ");
            sb.append(entry.getKey().getClass().getSimpleName());
            sb.append(" time in test: ");
            sb.append( (entry.getValue() / 1_000_000_000d) );
            sb.append(" seconds ---- ");
            results.add(new Result(sb.toString(), entry.getValue()));
            testTime += entry.getValue();
            cacheNum++;
        }
        double onePercent = testTime / 100d;


        StringBuilder allResult = new StringBuilder("Cache test \n");
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
    public void setUp() throws CacheException {
        Iterator<Cache> iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            Cache cache = iterator.next();
            long timeBefore = System.nanoTime();
            for (int i = 0; i < (cache.maxSize() - 1); i++) {
                cache.put(new TestCacheData(i, "testCashData" + i));
            }
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheMap.get(cache);
            cacheMap.put(cache, oldResult + resultTime);
        }
    }

    @After
    public void tearDown() throws CacheException {
        Iterator<Cache> iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            Cache cache = iterator.next();
            long timeBefore = System.nanoTime();
            cache.clear();
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheMap.get(cache);
            cacheMap.put(cache, oldResult + resultTime);
        }
    }

    @Test
    public void testPutNull() throws CacheException {
        Iterator<Cache> iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            Cache cache = iterator.next();
            long timeBefore = System.nanoTime();
            cache.put(null);
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheMap.get(cache);
            cacheMap.put(cache, oldResult + resultTime);
        }
        iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            Cache cache = iterator.next();
            long timeBefore = System.nanoTime();
            cache.put(new TestCacheData(32545435, null));
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheMap.get(cache);
            cacheMap.put(cache, oldResult + resultTime);
        }
    }

    @Test
    public void testGetNotExist() throws CacheException {
        Iterator<Cache> iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            Cache cache = iterator.next();
            long timeBefore = System.nanoTime();
            cache.get(324324);
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheMap.get(cache);
            cacheMap.put(cache, oldResult + resultTime);
        }
    }

    @Test
    public void testPutUnique() throws CacheException, CacheLevelException {
        Iterator<Cache> iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            Cache cache = iterator.next();
            int cacheMaxSize = cache.maxSize();
            long timeBefore = System.nanoTime();
            cache.put(new TestCacheData(cacheMaxSize - 1, "testCashData" + (cacheMaxSize - 1)));
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheMap.get(cache);
            cacheMap.put(cache, oldResult + resultTime);
            int cacheSize = cache.size();
            assertEquals("Unique item was pasted. Cache must be full and contains " + (cacheMaxSize) + ", but contains " + cacheSize + ". \n" + cache.toString(), true, cache.isFull());
        }
    }

    @Test
    public void testPutNotUnique() throws CacheException, CacheLevelException {
        Iterator<Cache> iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            Cache cache = iterator.next();
            int cacheSize = cache.size();
            long timeBefore = System.nanoTime();
            cache.put(new TestCacheData(0, "testCashData" + 0));
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheMap.get(cache);
            cacheMap.put(cache, oldResult + resultTime);
            int newCacheSize = cache.size();
            assertEquals("Not unique item was pasted. Cache must be " + cacheSize + ", but is " + newCacheSize + ". \n" + cache.toString(), cacheSize, newCacheSize);
        }
    }

    @Test
    public void testGet() throws CacheException {
        TestCacheData storedData = new TestCacheData(0, "testCashData0");

        Iterator<Cache> iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            Cache cache = iterator.next();
            long timeBefore = System.nanoTime();
            TestCacheData cashedData = (TestCacheData) cache.get(0);
            long resultTime = System.nanoTime() - timeBefore;
            long oldResult = cacheMap.get(cache);
            cacheMap.put(cache, oldResult + resultTime);
            assertEquals("Cannot get correct item.", storedData, cashedData);
        }
    }
}