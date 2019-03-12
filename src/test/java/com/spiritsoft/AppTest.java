package com.spiritsoft;

import com.spiritsoft.throttle.exceptions.RequestBlockedException;
import com.spiritsoft.throttle.implementation.ConfigurationServiceImpl;
import com.spiritsoft.throttle.implementation.RateLimitsCache;
import com.spiritsoft.throttle.implementation.RateLimitsMaster;
import com.spiritsoft.throttle.model.ConfigurationTree;
import com.spiritsoft.throttle.model.config.ThrottleConfiguration;
import com.spiritsoft.throttle.model.runtime.ThrottleLimits;
import com.spiritsoft.throttle.service.AccountToSegmentCache;
import com.spiritsoft.throttle.service.ConfigurationService;
import com.spiritsoft.throttle.service.RateLimiterService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit test for simple App.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppTest 
{
    @Mock
    AccountToSegmentCache segmentLookupCache;

    ConfigurationService configService;

    @Before
    public void setup() {
        when(segmentLookupCache.getSegment("cisco")).thenReturn("premium");
        when(segmentLookupCache.getSegment("intel")).thenReturn("freeSegment");
        //let's say ola is mapped to segment that does not have any Special setting
        when(segmentLookupCache.getSegment("ola")).thenReturn("newSegment");

        configService = new ConfigurationServiceImpl(segmentLookupCache);
        InputStream stream = AppTest.class.getClassLoader().getResourceAsStream("configuration.json");
        configService.init(stream);
    }


    @Test
    public void testConfigurationRead() {
        List<ThrottleConfiguration> configs = configService.get();
        assertEquals(3, configs.size());
    }

    @Test
    public void testResourceConfigMap() {
        Map<String, ConfigurationTree> resourceConfigMap = configService.getResourceConfigMap();
        ConfigurationTree t1 = resourceConfigMap.get("restrictedResource");
        //assert that it has 2 segment children at first level
        assertEquals(2, t1.getChildren().size());
        //since cisco is in premium segment this tree should have 1 account child at second level
        //under segment premium and its name should be cisco
        for (ConfigurationTree t2 : t1.getChildren()) {
            if (t2.getName().equals("premium")) {
                assertEquals(1, t2.getChildren().size());
                for (ConfigurationTree t3: t2.getChildren()) {
                    assertEquals("cisco", t3.getName());
                }
            }
        }

    }

    @Test
    public void testInferredMasterRateLimits() {
        //validate the inferred : number of requests per second
        //given the configuration JSON
        //1. resource="newResource", account="cisco", segment="premium", result=200
        //2. resource="expensiveResource", account="ola", segment="newSegment", result=10
        //3. resource="expensiveResource", account="anz", segment="testSegment", result=2
        //4. resource="restrictedResource", account="intel", segment="freeSegment", result=2
        //5. resource="restrictedResource", account="ola", segment="newSegment", result=10
        //6. resource="expensiveResource", account="intel", segment="freeSegment", result=0

        RateLimitsMaster master = new RateLimitsMaster(configService);
        ThrottleLimits result1 = master.get("cisco",
                segmentLookupCache.getSegment("cisco"), "newResource");
        assertEquals(200, result1.getAllowedOriginal());

        ThrottleLimits result2 = master.get("ola",
                segmentLookupCache.getSegment("ola"), "expensiveResource");
        assertEquals(10, result2.getAllowedOriginal());

        //account : anz is not in any segment
        ThrottleLimits result3 = master.get("anz",
                segmentLookupCache.getSegment("anz"), "expensiveResource");
        assertEquals(2, result3.getAllowedOriginal());

        //since intel is in freesegment the result should be 2
        ThrottleLimits result4 = master.get("intel",
                segmentLookupCache.getSegment("intel"), "restrictedResource");
        assertEquals(2, result4.getAllowedOriginal());

        ThrottleLimits result5 = master.get("ola",
                segmentLookupCache.getSegment("ola"), "restrictedResource");
        assertEquals(10, result5.getAllowedOriginal());

        //since intel is free segment, its not allowed access to expensiveResource
        ThrottleLimits result6 = master.get("intel",
                segmentLookupCache.getSegment("intel"), "expensiveResource");
        assertEquals(0, result6.getAllowedOriginal());

    }

    @Test
    public void testInferredMasterRateLimits_SecondCallFromCache() {
        // TODO:  verify the number of calls to private method
        //computeRateLimit is only 1 even though we call get method twice.
        //need to use PowerMockito for this.

        RateLimitsMaster master = new RateLimitsMaster(configService);
        ThrottleLimits result1 = master.get("cisco",
                segmentLookupCache.getSegment("cisco"), "newResource");

        ThrottleLimits result2 = master.getMap().get("newResource:cisco");
        assertEquals(result1, result2);
    }

    @Test(expected= RequestBlockedException.class)
    public void testRateLimiting() {

        RateLimitsMaster master = new RateLimitsMaster(configService);
        RateLimiterService cache = new RateLimitsCache(master);
        //if intel account tries to access restrictedResource 3 times in quick succession
        //the third call should result in RequestBlocked.
        cache.get("intel",
                segmentLookupCache.getSegment("intel"), "restrictedResource");
        cache.get("intel",
                segmentLookupCache.getSegment("intel"), "restrictedResource");
        cache.get("intel",
                segmentLookupCache.getSegment("intel"), "restrictedResource");

    }

    @Test
    public void testRateLimiting_Refresh() {

        RateLimitsMaster master = new RateLimitsMaster(configService);
        RateLimiterService cache = new RateLimitsCache(master);
        //if intel account tries to access restrictedResource 3 times in quick succession
        //the third call should result in RequestBlocked.
        cache.get("intel",
                segmentLookupCache.getSegment("intel"), "restrictedResource");
        cache.get("intel",
                segmentLookupCache.getSegment("intel"), "restrictedResource");

        //now the limit of requests per second has hit. So wait for a second and try again
        //it should succeed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cache.get("intel",
                segmentLookupCache.getSegment("intel"), "restrictedResource");
        cache.get("intel",
                segmentLookupCache.getSegment("intel"), "restrictedResource");

    }

    @Test(timeout = 16) //milliseconds
    public void testRateLimiting_Performance() {

        RateLimitsMaster master = new RateLimitsMaster(configService);
        RateLimiterService cache = new RateLimitsCache(master);
        for (int i=0; i < 200; i++) {
            int result1 = cache.get("cisco",
                    segmentLookupCache.getSegment("cisco"), "newResource");
        }
    }

    @Test
    public void testRateLimiting_ConcurrentAccess() {

        //there should be no exceptions thrown
        RateLimitsMaster master = new RateLimitsMaster(configService);
        RateLimiterService cache = new RateLimitsCache(master);
        //resource="expensiveResource", account="ola", segment="newSegment", result=10

        ExecutorService WORKER_THREAD_POOL
                = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            WORKER_THREAD_POOL.submit(() -> {
                try {
                    int result1 = cache.get("ola",
                            segmentLookupCache.getSegment("ola"), "expensiveResource");
                    System.out.println(result1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // wait for the latch to be decremented by the two remaining threads
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            WORKER_THREAD_POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }


}
