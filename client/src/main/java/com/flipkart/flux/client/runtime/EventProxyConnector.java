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
package com.flipkart.flux.client.runtime;

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.client.config.FluxClientConfiguration;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.SharedMetricRegistries;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventProxyConnector extends FluxRuntimeConnectorHttpImpl {

    public static Logger logger = LogManager.getLogger(EventProxyConnector.class);
    private static final String EXTERNAL = "external";

    @Inject
    public EventProxyConnector(@Named("eventProxyForMigration.endpoint") String endpoint, FluxClientConfiguration fluxClientConfiguration) {
        super(fluxClientConfiguration.getConnectionTimeout(), fluxClientConfiguration.getSocketTimeout(),
                endpoint, new ObjectMapper(), SharedMetricRegistries.getOrCreate("mainMetricRegistry"));
    }

    @Override
    public void submitEvent(String name, Object data, String correlationId, String eventSource) {
        final String eventType = data.getClass().getName();
        if (eventSource == null) {
            eventSource = EXTERNAL;
        }
        CloseableHttpResponse httpResponse = null;
        try {
            final EventData eventData = new EventData(name, eventType, (String) data, eventSource);
            httpResponse = postOverHttp(eventData, "/" + correlationId + "/context/events?searchField=correlationId");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
    }

    @Override
    public void submitScheduledEvent(String name, Object data, String correlationId, String eventSource, Long triggerTime) {
        final String eventType = data.getClass().getName();
        if (eventSource == null) {
            eventSource = EXTERNAL;
        }
        final EventData eventData = new EventData(name, eventType, (String) data, eventSource);
        CloseableHttpResponse httpResponse = null;
        try {
            if (triggerTime != null) {
                httpResponse = postOverHttp(eventData, "/" + correlationId + "/context/events?searchField=correlationId&triggerTime=" + triggerTime);
            } else {
                //this block is used by flux to trigger the event when the time has arrived, send the data as plain string without serializing,
                // as the data is already in serialized form (in ScheduledEvents table the data stored in serialized form)
                httpResponse = postOverHttp(eventData, "/" + correlationId + "/context/events?searchField=correlationId");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
    }

}


















