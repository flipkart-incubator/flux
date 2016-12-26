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
import com.flipkart.flux.api.ExecutionUpdateData;
import com.flipkart.flux.api.StateMachineDefinition;
import com.google.common.annotations.VisibleForTesting;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(FluxRuntimeConnectorHttpImpl.class);

    public static final int MAX_TOTAL = 200;
    public static final int MAX_PER_ROUTE = 20;
    public static final String EXTERNAL = "external";
    private final CloseableHttpClient closeableHttpClient;
    private final String fluxEndpoint;
    private final ObjectMapper objectMapper;

    @VisibleForTesting
    public FluxRuntimeConnectorHttpImpl(Long connectionTimeout, Long socketTimeout, String fluxEndpoint) {
        this(connectionTimeout, socketTimeout, fluxEndpoint, new ObjectMapper());
    }

    public FluxRuntimeConnectorHttpImpl(Long connectionTimeout, Long socketTimeout, String fluxEndpoint, ObjectMapper objectMapper) {
        this.fluxEndpoint = fluxEndpoint;
        this.objectMapper = objectMapper;
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
            if(logger.isDebugEnabled()) {
                try {
                    logger.debug("Flux returned response: {}", EntityUtils.toString(httpResponse.getEntity()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
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

	/**
	 * Interface method implementation. Updates the status in persistence store by invoking suitable Flux runtime API
	 * @see com.flipkart.flux.client.runtime.FluxRuntimeConnector#updateExecutionStatus(ExecutionUpdateData)
	 */
	public void updateExecutionStatus(ExecutionUpdateData executionUpdateData) {
		CloseableHttpResponse httpResponse = null;
        httpResponse = postOverHttp(executionUpdateData,  "/" + executionUpdateData.getStateMachineId() + "/" + executionUpdateData.getTaskId() + "/status");
        HttpClientUtils.closeQuietly(httpResponse);
	}

	/**
	 * Interface method implementation. Increments the execution retries in persistence by invoking suitable Flux runtime API
	 * @see com.flipkart.flux.client.runtime.FluxRuntimeConnector#incrementExecutionRetries(java.lang.Long, java.lang.Long)
	 */
	@Override
	public void incrementExecutionRetries(Long stateMachineId,Long taskId) {
		CloseableHttpResponse httpResponse = null;
        httpResponse = postOverHttp(null,  "/" + stateMachineId + "/" + taskId + "/retries/inc");
        HttpClientUtils.closeQuietly(httpResponse);
	}

    /**
     * Interface method implementation. Posts to Flux Runtime API to redrive a task.
     */
    @Override
    public void redriveTask(Long taskId) {
        CloseableHttpResponse httpResponse = null;
        httpResponse = postOverHttp(null,  "/redrivetask/" + taskId);
        HttpClientUtils.closeQuietly(httpResponse);
    }
	
	/** Helper method to post data over Http */
    private CloseableHttpResponse postOverHttp(Object dataToPost, String pathSuffix)  {
        CloseableHttpResponse httpResponse = null;
        HttpPost httpPostRequest;
        httpPostRequest = new HttpPost(fluxEndpoint + pathSuffix);
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            objectMapper.writeValue(byteArrayOutputStream, dataToPost);
            httpPostRequest.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray(), ContentType.APPLICATION_JSON));
            httpResponse = closeableHttpClient.execute(httpPostRequest);
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode >= Response.Status.OK.getStatusCode() && statusCode < Response.Status.MOVED_PERMANENTLY.getStatusCode() ) {
                logger.trace("Posting over http is successful. Status code: {}", statusCode);
            } else {
                logger.error("Did not receive a valid response from Flux core. Status code: {}, message: {}", statusCode, EntityUtils.toString(httpResponse.getEntity()));
                HttpClientUtils.closeQuietly(httpResponse);
                throw new RuntimeCommunicationException("Did not receive a valid response from Flux core");
            }
        } catch (IOException e) {
            logger.error("Posting over http errored. Message: {}", e.getMessage(), e);
            HttpClientUtils.closeQuietly(httpResponse);
            throw new RuntimeCommunicationException("Could not communicate with Flux runtime: " + fluxEndpoint);
        }
        return httpResponse;
    }

}
