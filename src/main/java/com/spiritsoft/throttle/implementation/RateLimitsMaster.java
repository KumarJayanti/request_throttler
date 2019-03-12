package com.spiritsoft.throttle.implementation;

import com.spiritsoft.throttle.exceptions.ConfigurationValidationException;
import com.spiritsoft.throttle.model.ConfigurationTree;
import com.spiritsoft.throttle.model.runtime.ThrottleLimits;
import com.spiritsoft.throttle.service.ConfigurationService;
import com.spiritsoft.throttle.service.DistributedCacheFactory;

import java.util.Map;

/**
 *
 */
public class RateLimitsMaster {

    private final ConfigurationService configurationService;

    //lazily construct the ThrottleLimits for each resource and return.
    //the Key here is "resource:account"
    private Map<String, ThrottleLimits> map =
            DistributedCacheFactory.getInstance().getCache();

    public RateLimitsMaster(ConfigurationService configService) {
        this.configurationService = configService;
    }

    public static String computeKey(String accountId, String resource) {
        return resource + ":" + accountId;
    }

    public ThrottleLimits get(String accountId, String segment, String resource) {
        String key = computeKey(accountId, resource);
        if (map.containsKey(key)) {
            return map.get(key);
        }
        //compute the ThrottleLimits and put into the map before returning
        Map<String, ConfigurationTree>  resourceConfigMap =
                configurationService.getResourceConfigMap();

        String res = resourceConfigMap.containsKey(resource) ?
                resource : ConfigurationService.GLOBAL_RESOURCE;
        ConfigurationTree tree = resourceConfigMap.get(res);
        if (tree == null) {
            throw new ConfigurationValidationException(
                    "Could not find mandatory throttle limit configuration for resource:"
                            + ConfigurationService.GLOBAL_RESOURCE);
        }
        int limit = computeRateLimit(tree, segment, accountId);
        ThrottleLimits tl = new ThrottleLimits(limit);
        map.put(key, tl);

        return tl;
    }

    private  int computeRateLimit(ConfigurationTree tree, String segment, String account) {
        if (tree == null) {
            throw new IllegalArgumentException("Unexpected null Configuration Tree");
        }
        //start with the value at the root.
        int rateLimit = tree.getValue();
        //now check if there is a segment level override
        ConfigurationTree segmentSubtree = null;
        for (ConfigurationTree t : tree.getChildren()) {
            if (t.getName().equals(segment)) {
                segmentSubtree = t;
                rateLimit = t.getValue();
            } else if (t.getName().equals(account)) {
                rateLimit = t.getValue();
            }
        }
        //now check if there is an account level override under the segment
        if (segmentSubtree != null) {
            for (ConfigurationTree t : segmentSubtree.getChildren()) {
                if (t.getName().equals(account)) {
                    rateLimit = t.getValue();
                }
            }
        }

        return rateLimit;
    }

    //adding a getMap just for JUnit testing purposes
    //Not to be used otherwise
    public Map<String, ThrottleLimits> getMap() {
        return map;
    }
}
