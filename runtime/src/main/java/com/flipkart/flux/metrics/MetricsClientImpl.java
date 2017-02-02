/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.metrics;

import com.codahale.metrics.MetricRegistry;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.google.inject.Inject;

/**
 * <code>MetricsClientImpl</code> implements <a href="http://metrics.dropwizard.io">Metrics</a> library {@link MetricsClient} and publishes metrics using {@link MetricRegistry}.
 * @author kaushal.hooda
 */
public class MetricsClientImpl implements MetricsClient {

    /**
     * Stores all the metrics.
     */
    private final MetricRegistry metricRegistry;

    @Inject
    MetricsClientImpl(MetricRegistry metricRegistry){
        this.metricRegistry = metricRegistry;
    }

    /**
     * Creates/updates a meter with the name key.
     * @see {@link com.codahale.metrics.Meter}
     * @param key Specifies the type of event.
     */
    @Override
    public void markMeter(String key) {
        metricRegistry.meter(key).mark();
    }
}
