package com.flipkart.flux.metrics.impl;

import com.codahale.metrics.MetricRegistry;
import com.flipkart.flux.metrics.interfaces.MetricsClient;
import com.google.inject.Inject;

/**
 * Created by kaushal.hooda on 25/01/17.
 */
public class MetricsClientImpl implements MetricsClient {

    private final MetricRegistry metricRegistry;

    @Inject
    MetricsClientImpl(MetricRegistry metricRegistry){
        this.metricRegistry = metricRegistry;
    }



    @Override
    public void incrCount(String key) {
        metricRegistry.counter(key).inc();
    }

    @Override
    public void decrCount(String key) {
        metricRegistry.counter(key).dec();
    }

    @Override
    public void markEvent(String key) {
        metricRegistry.meter(key).mark();
    }
}
