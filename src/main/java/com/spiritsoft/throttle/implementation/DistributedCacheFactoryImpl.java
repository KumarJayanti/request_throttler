package com.spiritsoft.throttle.implementation;

import com.spiritsoft.throttle.service.DistributedCacheFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stubbed Implementation of Distributed Cache Factory that just uses a local hashmap.
 */
public class DistributedCacheFactoryImpl extends DistributedCacheFactory {

    @Override
    public <K, V> Map<K, V> getCache() {
        return new ConcurrentHashMap<>();
    }
}