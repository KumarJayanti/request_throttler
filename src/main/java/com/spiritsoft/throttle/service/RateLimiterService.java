package com.spiritsoft.throttle.service;

import com.spiritsoft.throttle.exceptions.RequestBlockedException;


public interface RateLimiterService {

    /**
     * Ideally resource should be an object to allow for flexibility in resource
     * definition and applicable limit settings at subresource levels, but for now
     * we have a resource as an indivisible opaque String.
     * @param accountId
     * @param segment
     * @param resource
     * @return a count of remaining requests allowed
     * @throws {@link RequestBlockedException} if the request is blocked due to rateLimit
     */
    int get(String accountId, String segment, String resource);

}
