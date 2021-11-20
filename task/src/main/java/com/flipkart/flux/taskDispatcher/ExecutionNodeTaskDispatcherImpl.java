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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.core.TaskExecutionMessage;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.google.inject.Inject;

@Singleton
public class ExecutionNodeTaskDispatcherImpl implements ExecutionNodeTaskDispatcher {

    private static Logger logger = LogManager.getLogger(ExecutionNodeTaskDispatcherImpl.class);
    private final CloseableHttpClient closeableHttpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MetricsClient metricsClient;


    @Inject
    public ExecutionNodeTaskDispatcherImpl(@Named("connector.max.connections") Integer maxConnections, @Named("connector.max.connections.per.route") Integer maxConnectionsPerRoute,
                                           @Named("connector.connection.timeout") Integer connectionTimeout, @Named("connector.socket.timeout") Integer socketTimeOut,
                                           MetricsClient metricsClient) {
        RequestConfig clientConfig = RequestConfig.custom()
                .setConnectTimeout((connectionTimeout).intValue())
                .setSocketTimeout((socketTimeOut).intValue())
                .setConnectionRequestTimeout((socketTimeOut).intValue())
                .build();
        PoolingHttpClientConnectionManager syncConnectionManager = new PoolingHttpClientConnectionManager();
        syncConnectionManager.setMaxTotal(maxConnections);
        syncConnectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
        closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(clientConfig).setConnectionManager(syncConnectionManager)
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HttpClientUtils.closeQuietly(closeableHttpClient);
        }));
        this.metricsClient = metricsClient;
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
            logger.error("Posting over http errored. smId: {}, taskId:{} Message:{}  Exception: {}",
                    taskExecutionMessage.getAkkaMessage().getStateMachineId(),
                    taskExecutionMessage.getAkkaMessage().getTaskId(),
                    e.getMessage(), e);
        } finally {
            /* 200 <= defaultStatusCode < 301 */
            if (defaultStatusCode >= Response.Status.OK.getStatusCode()
                    && defaultStatusCode < Response.Status.MOVED_PERMANENTLY.getStatusCode()) {
                metricsClient.markMeter(new StringBuilder().
                        append("stateMachine.tasks.forwardToExecutor.2xx").toString());
            }
            /* 400 <= defaultStatusCode < 500 */
            else if (defaultStatusCode >= Response.Status.BAD_REQUEST.getStatusCode()
                    && defaultStatusCode < Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
                metricsClient.markMeter(new StringBuilder().
                        append("stateMachine.tasks.forwardToExecutor.4xx").toString());
            }
            /* 500 <= defaultStatusCode <= 505 */
            else if (defaultStatusCode >= Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()
                    && defaultStatusCode < Response.Status.HTTP_VERSION_NOT_SUPPORTED.getStatusCode()) {
                metricsClient.markMeter(new StringBuilder().
                        append("stateMachine.tasks.forwardToExecutor.5xx").toString());
            }

        }
        HttpClientUtils.closeQuietly(httpResponse);
        return defaultStatusCode;
    }

}
