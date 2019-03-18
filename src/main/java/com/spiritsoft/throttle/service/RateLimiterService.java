package com.spiritsoft.throttle.service;

import com.spiritsoft.throttle.exceptions.RequestBlockedException;
import com.spiritsoft.throttle.model.runtime.ThrottleLimits;


public interface RateLimiterService {

    /**
     * Ideally resource should be an object to allow for flexibility in resource
     * definition and applicable limit settings at subresource levels, but for now
     * we have a resource as an indivisible opaque String.
     * @param accountId
     * @param segment
     * @param resource
     * @return The ThrottleLimits object
     * @throws {@link RequestBlockedException} if the request is blocked due to rateLimit
     */
    ThrottleLimits get(String accountId, String segment, String resource);

}
