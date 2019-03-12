package com.spiritsoft.throttle.model.config;

public class ThrottleConfiguration
{
    private String name;

    private ThrottleSettings throttleSettings;

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public ThrottleSettings getThrottleSettings ()
    {
        return throttleSettings;
    }

    public void setThrottleSettings (ThrottleSettings throttleSettings)
    {
        this.throttleSettings = throttleSettings;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [name = "+name+", throttleSettings = "+throttleSettings+"]";
    }
}