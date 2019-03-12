package com.spiritsoft.throttle.model.config;

public class Segment
{
    private String name;

    private int requestsPerSecond;

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public int getRequestsPerSecond ()
    {
        return requestsPerSecond;
    }

    public void setRequestsPerSecond (int requestsPerSecond)
    {
        this.requestsPerSecond = requestsPerSecond;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [name = "+name+", requestsPerSecond = "+requestsPerSecond+"]";
    }
}