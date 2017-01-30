package com.flipkart.flux.metrics.interfaces;

/**
 * Created by kaushal.hooda on 25/01/17.
 */
public interface MetricsClient {

    public void incrCount(String key);

    public void decrCount(String key);

    public void markEvent(String key);

}
