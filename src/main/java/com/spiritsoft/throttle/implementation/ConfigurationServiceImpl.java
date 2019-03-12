package com.spiritsoft.throttle.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spiritsoft.throttle.exceptions.ConfigurationValidationException;
import com.spiritsoft.throttle.model.ConfigurationTree;
import com.spiritsoft.throttle.model.config.Account;
import com.spiritsoft.throttle.model.config.Segment;
import com.spiritsoft.throttle.model.config.ThrottleConfiguration;
import com.spiritsoft.throttle.service.AccountToSegmentCache;
import com.spiritsoft.throttle.service.ConfigurationService;
import com.spiritsoft.throttle.service.DistributedCacheFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationServiceImpl implements ConfigurationService {

    private AccountToSegmentCache accountToSegmentCacheService;

    private List<ThrottleConfiguration> configurations = new ArrayList<>();

    //map of resource/global to corresponding configurationTree
    private Map<String, ConfigurationTree> resourceConfigMap =
            DistributedCacheFactory.getInstance().getCache();

    //Configuration uses a dummy global resource identifier to signify global ThrottleLimits
    public static final String GLOBAL_RESOURCE = "globalDefault";

    private ObjectMapper mapper = new ObjectMapper(); // create once, reuse

    public ConfigurationServiceImpl(AccountToSegmentCache segmentLookupCache) {
        this.accountToSegmentCacheService = segmentLookupCache;
    }

    @Override
    public void init(String configuration) {

        try {
            configurations =  mapper.readValue(configuration, new TypeReference<List<ThrottleConfiguration>>(){});

        } catch (IOException e) {
            e.printStackTrace();
            throw new ConfigurationValidationException(e);
        }
        computeTransformedConfigurations();
    }

    /**
     * TODO: more semantic validations need to be done on the Configuration Source.
     * Such as minimum 1 mandatory Configuration to be present always corresponding to
     * Global Config.
     *
     * OR
     * If there is no Global Config then no RateLimits ?.
     * @param configuration
     */
    @Override
    public void init(InputStream configuration) {
        try {
            configurations =  mapper.readValue(configuration, new TypeReference<List<ThrottleConfiguration>>(){});
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConfigurationValidationException(e);
        }
        computeTransformedConfigurations();
    }

    @Override
    public List<ThrottleConfiguration> get() {
        return configurations;
    }

    @Override
    public Map<String, ConfigurationTree> getResourceConfigMap() {
        return resourceConfigMap;
    }

    private void computeTransformedConfigurations() {

        for (ThrottleConfiguration c : configurations) {
            ConfigurationTree root;
            //get the resource name
            String name = c.getName();
            int value = c.getThrottleSettings().getRequestsPerSecond();
            root = new ConfigurationTree(name, value);
            Map<String, ConfigurationTree>  segmentMap = new HashMap<>();
            for (Segment s : c.getThrottleSettings().getSegment()) {
                name = s.getName();
                value = s.getRequestsPerSecond();
                ConfigurationTree child = root.addChild(name, value);
                segmentMap.put(name, child);
            }

            for (Account a : c.getThrottleSettings().getAccount()) {
                name = a.getName();
                value = a.getRequestsPerSecond();
                String parentSegment = accountToSegmentCacheService.getSegment(name);
                ConfigurationTree parent = segmentMap.get(parentSegment);
                if (parent == null) {
                    //add it directly to root
                    root.addChild(name, value);
                } else {
                    parent.addChild(name, value);
                }
            }
            //add the tree rooted at root to the map
            resourceConfigMap.put(c.getName(), root);
        }

    }


}
