package com.flipkart.flux.client.runtime;

public interface FluxHttpClient {

    void postOverHttp(Object dataToPost, String pathSuffix);
}
