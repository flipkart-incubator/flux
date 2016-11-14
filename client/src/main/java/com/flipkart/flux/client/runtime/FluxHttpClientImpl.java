package com.flipkart.flux.client.runtime;

import com.google.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static javax.ws.rs.core.Response.Status.OK;

public class FluxHttpClientImpl implements FluxHttpClient {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(FluxHttpClientImpl.class);

    private final ObjectMapper objectMapper;
    private final String fluxEndpoint;
    private final CloseableHttpClient httpClient;

    @Inject
    public FluxHttpClientImpl(ObjectMapper objectMapper, String fluxEndpoint,
                              CloseableHttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.fluxEndpoint = fluxEndpoint;
        this.httpClient = httpClient;
    }

    @Override
    public void postOverHttp(Object dataToPost, String pathSuffix) {
        CloseableHttpResponse httpResponse = null;
        HttpPost httpPostRequest;
        httpPostRequest = new HttpPost(fluxEndpoint + pathSuffix);
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            objectMapper.writeValue(byteArrayOutputStream, dataToPost);
            httpPostRequest.setEntity(new ByteArrayEntity(byteArrayOutputStream.toByteArray(),
                                                          ContentType.APPLICATION_JSON));
            httpResponse = httpClient.execute(httpPostRequest);
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode >= OK.getStatusCode() && statusCode < MOVED_PERMANENTLY
                .getStatusCode()) {
                LOGGER.debug("got a 2xx from {}", httpResponse.getStatusLine().getStatusCode());
            } else {
                LOGGER.warn("encountered a non 2xx status code {}",
                            httpResponse.getStatusLine().getStatusCode());
                throw new RuntimeCommunicationException(
                    "Did not receive a valid response from Flux core");
            }
        } catch (IOException e) {
            int statusCode = Optional.ofNullable(httpResponse)
                .map(HttpResponse::getStatusLine)
                .map(StatusLine::getStatusCode)
                .orElse(0);
            LOGGER.error("encountered an error while connecting to flux. Possible status {}. "
                         + "Exception message {}", statusCode, e.getMessage());
            LOGGER.debug("stack trace", e);
            throw new RuntimeCommunicationException("Could not communicate with Flux runtime: "
                                                    + fluxEndpoint);
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
    }
}
