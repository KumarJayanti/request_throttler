package com.spiritsoft.throttle.service;

import com.spiritsoft.throttle.implementation.DistributedCacheFactoryImpl;

import java.util.Map;


public abstract class DistributedCacheFactory {

    public abstract <K,V> Map<K, V> getCache();

    public static  DistributedCacheFactory getInstance() {
        return new DistributedCacheFactoryImpl();
    }
}
