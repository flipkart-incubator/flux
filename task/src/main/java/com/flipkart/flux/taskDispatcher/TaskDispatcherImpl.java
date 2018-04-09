/*
 * Copyright 2012-2018, the original author or authors.
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

package com.flipkart.flux.taskDispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.core.TaskExecutionMessage;
import com.google.inject.Inject;
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

import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Singleton
public class TaskDispatcherImpl implements TaskDispatcher {

    private static Logger logger = LoggerFactory.getLogger(TaskDispatcherImpl.class);
    //Default , should be overridden via config if necessary
    // Need to add a metric for this api as well
    public static final int MAX_TOTAL_CONNECTIONS = 100;
    public static final int MAX_CONNECTIONS_PER_ROUTE = 25;
    public static final Long connectionTimeOut = 10000L;
    public static final Long socketTimeOut = 10000L;


    private final CloseableHttpClient closeableHttpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public TaskDispatcherImpl() {
        RequestConfig clientConfig = RequestConfig.custom()
                .setConnectTimeout((TaskDispatcherImpl.connectionTimeOut).intValue())
                .setSocketTimeout((TaskDispatcherImpl.socketTimeOut).intValue())
                .setConnectionRequestTimeout((TaskDispatcherImpl.socketTimeOut).intValue())
                .build();
        PoolingHttpClientConnectionManager syncConnectionManager = new PoolingHttpClientConnectionManager();
        syncConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        syncConnectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(clientConfig).setConnectionManager(syncConnectionManager)
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HttpClientUtils.closeQuietly(closeableHttpClient);
        }));
    }


    @Override
    public int forwardExecutionMessage(String endPoint, TaskExecutionMessage taskExecutionMessage) {
        int defaultStatusCode = -1;
        CloseableHttpResponse httpResponse = null;
        HttpPost httpPostRequest;
        httpPostRequest = new HttpPost(endPoint);
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            objectMapper.writeValue(byteArrayOutputStream, taskExecutionMessage);

            httpPostRequest.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray(), ContentType.APPLICATION_JSON));
            httpResponse = closeableHttpClient.execute(httpPostRequest);
            defaultStatusCode = httpResponse.getStatusLine().getStatusCode();
            if (defaultStatusCode == Response.Status.ACCEPTED.getStatusCode()) {
                logger.info("Posting over http is successful. StatusCode: {} smId:{} taskId:{}", defaultStatusCode,
                        taskExecutionMessage.getAkkaMessage().getStateMachineId(),
                        taskExecutionMessage.getAkkaMessage().getTaskId());
            } else {
                logger.error("Did not receive a valid response from Flux core. StatusCode: {}, smId:{} taskId:{} message: {}",
                        defaultStatusCode,
                        taskExecutionMessage.getAkkaMessage().getStateMachineId(),
                        taskExecutionMessage.getAkkaMessage().getTaskId(),
                        EntityUtils.toString(httpResponse.getEntity()));
            }
        } catch (IOException e) {
            logger.error("Posting over http errored. StatusCode: {}, smId:{} taskId:{}  Message: {}",
                    taskExecutionMessage.getAkkaMessage().getStateMachineId(),
                    taskExecutionMessage.getAkkaMessage().getTaskId(),
                    e.getMessage(), e);
        }
        HttpClientUtils.closeQuietly(httpResponse);
        return defaultStatusCode;
    }

}
