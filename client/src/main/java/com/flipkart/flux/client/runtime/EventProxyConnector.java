package com.flipkart.flux.client.runtime;

import com.codahale.metrics.SharedMetricRegistries;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.client.config.FluxClientConfiguration;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EventProxyConnector extends FluxRuntimeConnectorHttpImpl {

    public static Logger logger = LoggerFactory.getLogger(EventProxyConnector.class);

    @Inject
    public EventProxyConnector(@Named("eventProxyForMigration.endpoint") String endpoint, FluxClientConfiguration fluxClientConfiguration) {
        super(fluxClientConfiguration.getConnectionTimeout(), fluxClientConfiguration.getSocketTimeout(),
                endpoint, new ObjectMapper(), SharedMetricRegistries.getOrCreate("mainMetricRegistry"), endpoint);
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


















