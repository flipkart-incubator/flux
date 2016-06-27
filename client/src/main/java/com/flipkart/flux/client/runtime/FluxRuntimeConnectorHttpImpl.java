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
 *
 */

package com.flipkart.flux.client.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.StateMachineDefinition;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * RuntimeConnector that connects to runtime over HTTP
 * Internally, this uses Unirest as of now. This makes it difficult to write unit tests for this class,
 * but it does very little so its okay
 * @author yogesh.nachnani
 */
public class FluxRuntimeConnectorHttpImpl implements FluxRuntimeConnector {

    public static final int MAX_TOTAL = 200;
    public static final int MAX_PER_ROUTE = 20;
    public static final String EXTERNAL = "external";
    private final CloseableHttpClient closeableHttpClient;
    private final String fluxEndpoint;
    private final ObjectMapper objectMapper;

    public FluxRuntimeConnectorHttpImpl(Long connectionTimeout, Long socketTimeout, String fluxEndpoint) {
        objectMapper = new ObjectMapper();
        this.fluxEndpoint = fluxEndpoint;
        RequestConfig clientConfig = RequestConfig.custom()
            .setConnectTimeout((connectionTimeout).intValue())
            .setSocketTimeout((socketTimeout).intValue())
            .setConnectionRequestTimeout((socketTimeout).intValue())
            .build();
        PoolingHttpClientConnectionManager syncConnectionManager = new PoolingHttpClientConnectionManager();
        syncConnectionManager.setMaxTotal(MAX_TOTAL);
        syncConnectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);

        closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(clientConfig).setConnectionManager(syncConnectionManager)
            .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HttpClientUtils.closeQuietly(closeableHttpClient);
        }));
    }

    @Override
    public void submitNewWorkflow(StateMachineDefinition stateMachineDef) {
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = postOverHttp(stateMachineDef, "");
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
    }

    private CloseableHttpResponse postOverHttp(Object dataToPost, String pathSuffix)  {
        CloseableHttpResponse httpResponse = null;
        HttpPost httpPostRequest;
        httpPostRequest = new HttpPost(fluxEndpoint + pathSuffix);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            objectMapper.writeValue(byteArrayOutputStream, dataToPost);
            httpPostRequest.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray(), ContentType.APPLICATION_JSON));
            httpResponse = closeableHttpClient.execute(httpPostRequest);
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode >= Response.Status.OK.getStatusCode() && statusCode < Response.Status.MOVED_PERMANENTLY.getStatusCode() ) {
                // all is well, TODO write a trace level log
            } else {
                // TODO: log status line here
                throw new RuntimeCommunicationException("Did not receive a valid response from Flux core");
            }
        } catch (IOException e) {
            // TODO log exception here
            e.printStackTrace();
            throw new RuntimeCommunicationException("Could not communicate with Flux runtime");
        }
        return httpResponse;
    }

    @Override
    public void submitEvent(EventData eventData, Long stateMachineId) {
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = postOverHttp(eventData, "/" + stateMachineId + "/context/events");
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
    }

    @Override
    public void submitEvent(String name,Object data, String correlationId,String eventSource) {
        final String eventType = data.getClass().getName();
        if (eventSource == null) {
            eventSource = EXTERNAL;
        }
        CloseableHttpResponse httpResponse = null;
        try {
            final EventData eventData = new EventData(name, eventType, objectMapper.writeValueAsString(data), eventSource);
            httpResponse = postOverHttp(eventData, "/" + correlationId + "/context/events?searchField=correlationId");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
    }
}
