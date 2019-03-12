package com.spiritsoft.throttle.implementation;

import com.spiritsoft.throttle.model.runtime.ThrottleLimits;
import com.spiritsoft.throttle.service.DistributedCacheFactory;
import com.spiritsoft.throttle.service.RateLimiterService;

import java.util.Map;

/**
 *
 */
public class RateLimitsCache implements RateLimiterService {
    RateLimitsMaster master;
    private static final  int MILLIS_IN_ONE_SECOND = 1000;

    //this is the cache that the RateLimiting Cache would use
    private Map<String, ThrottleLimits> map =
            DistributedCacheFactory.getInstance().getCache();


    public RateLimitsCache(RateLimitsMaster master) {
        this.master = master;
    }

    @Override
    public synchronized int get(String accountId, String segment, String resource) {
        String key = RateLimitsMaster.computeKey(accountId, resource);
        ThrottleLimits limits;
        if (map.containsKey(key)) {
            limits = map.get(key);
            if (limits.isTimedOut(MILLIS_IN_ONE_SECOND)) {
                limits = limits.timeStampedClone();
                map.put(key, limits);
            }
        } else {
            //need to fetch from the master
            //TODO: optimize this as currently we have compute key again inside master
            limits = master.get(accountId, segment, resource);
            //set the time of acquisition
            limits = limits.timeStampedClone();
            map.put(key, limits);

        }
        return limits.getIfAllowed();
    }

}
