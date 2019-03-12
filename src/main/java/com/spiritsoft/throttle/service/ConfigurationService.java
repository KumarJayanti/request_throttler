package com.spiritsoft.throttle.service;

import com.spiritsoft.throttle.model.ConfigurationTree;
import com.spiritsoft.throttle.model.config.ThrottleConfiguration;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ConfigurationService {

    //Configuration uses a dummy global resource identifier to signify global ThrottleLimits
    public static final String GLOBAL_RESOURCE = "globalDefault";

    /**
     *
     * @param configuration
     * @return ThrottleConfiguration
     * @throws  RuntimeException  {@link com.spiritsoft.throttle.exceptions.ConfigurationValidationException}
     */
    void init(String configuration);

    /**
     *
     * @param configuration
     * @return ThrottleConfiguration
     * @throws  RuntimeException  {@link com.spiritsoft.throttle.exceptions.ConfigurationValidationException}
     */
    void init(InputStream configuration);


    /**
     *
     * @return the Configuration
     */
    List<ThrottleConfiguration>  get();

    /**
     * A Map of resource+account versus the configuration tree.
     * @return
     */
    Map<String, ConfigurationTree> getResourceConfigMap();
}
