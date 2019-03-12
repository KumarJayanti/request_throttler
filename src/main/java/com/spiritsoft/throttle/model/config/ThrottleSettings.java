package com.spiritsoft.throttle.model.config;

public class ThrottleSettings
{
    private Segment[] segment;

    private int requestsPerSecond;

    private Account[] account;

    public Segment[] getSegment ()
    {
        return segment;
    }

    public void setSegment (Segment[] segment)
    {
        this.segment = segment;
    }

    public int getRequestsPerSecond ()
    {
        return requestsPerSecond;
    }

    public void setRequestsPerSecond (int requestsPerSecond)
    {
        this.requestsPerSecond = requestsPerSecond;
    }

    public Account[] getAccount ()
    {
        return account;
    }

    public void setAccount (Account[] account)
    {
        this.account = account;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [segment = "+segment+", requestsPerSecond = "+requestsPerSecond+", account = "+account+"]";
    }
}

