package com.spiritsoft.throttle.implementation;

import com.spiritsoft.throttle.exceptions.RequestBlockedException;
import com.spiritsoft.throttle.model.runtime.ThrottleLimits;
import com.spiritsoft.throttle.service.DistributedCacheFactory;
import com.spiritsoft.throttle.service.RateLimiterService;

import java.util.Map;

/**
 *
 */
public class RateLimitsCache implements RateLimiterService {
    RateLimitsMaster master;
    private static final int MILLIS_IN_ONE_SECOND = 1000;

    //this is the cache that the RateLimiting Cache would use
    private Map<String, ThrottleLimits> map =
            DistributedCacheFactory.getInstance().getCache();


    public RateLimitsCache(RateLimitsMaster master) {
        this.master = master;
    }

    @Override
    public ThrottleLimits get(String accountId, String segment, String resource) {
        String key = RateLimitsMaster.computeKey(accountId, resource);
        map.computeIfAbsent(key, (String k) ->
                master.get(accountId, segment, resource).timeStampedClone());

        ThrottleLimits ret =  map.computeIfPresent(key,
                    (k, v) -> {
                        if (v.isTimedOut(MILLIS_IN_ONE_SECOND)) {
                            return v.timeStampedClone();
                        } else {
                            ThrottleLimits rval=  v.getAndDecrementLimitsIfAllowed();
                            return  rval;
                        }
                    });

        return  ret;

    }

}
