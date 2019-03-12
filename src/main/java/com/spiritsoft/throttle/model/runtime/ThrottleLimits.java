package com.spiritsoft.throttle.model.runtime;

import com.spiritsoft.throttle.exceptions.RequestBlockedException;

import java.time.Instant;

import java.util.concurrent.atomic.AtomicInteger;

public class ThrottleLimits {

    private int allowedOriginal;
    private AtomicInteger allowedRequests = new AtomicInteger();
    private Instant time;

    public ThrottleLimits(int allowed) {
        this.allowedOriginal = allowed;
        this.allowedRequests.set(allowed);
        //by default its invalid time.
        this.time = null;
    }

    public int get() {
        return allowedRequests.get();
    }

    public int getIfAllowed() {
        if (allowedRequests.get() == 0) {
            throw new RequestBlockedException("Exceeded Allowed Requests Per Second, try again in some time");
        }
        return allowedRequests.getAndDecrement();
    }

    public ThrottleLimits timeStampedClone() {
        ThrottleLimits tl = new ThrottleLimits(allowedOriginal);
        tl.time = Instant.now();
        return tl;
    }

    public boolean isTimedOut(int timeLimitSeconds) {
         long timespan = Instant.now().toEpochMilli() - time.toEpochMilli();
         return (timespan >= timeLimitSeconds) ? true : false;
    }

    public int getAllowedOriginal() {
        return allowedOriginal;
    }

}
