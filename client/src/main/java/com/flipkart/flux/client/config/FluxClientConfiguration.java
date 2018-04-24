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

package com.flipkart.flux.client.config;

/**
 * <code>FluxClientConfiguration</code> provides configuration to connect to Flux runtime.
 * In production setup, client needs to have a provider for this class in one of his application's guice module.
 *
 * Example:
 * <code>
 *      {@literal @}Provides
 *      {@literal @}Singleton
 *      FluxClientConfiguration providesFluxClientConfiguration(AppConfiguration configuration) {
 *          return configuration.getFluxClientConfiguration();
 *      }
 * </code>
 *
 * @author ali.nalawala
 */
public class FluxClientConfiguration {

    private String fluxRuntimeUrl = "http://localhost:9998";
    private long socketTimeout = 10000;
    private long connectionTimeout = 10000;

    /** Constructors*/
    public FluxClientConfiguration() {}

    public FluxClientConfiguration(String fluxRuntimeUrl, long socketTimeout, long connectionTimeout) {
        this.fluxRuntimeUrl = fluxRuntimeUrl;
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
    }

    /** Accessor/Mutator methods*/
    public String getFluxRuntimeUrl() {
        return fluxRuntimeUrl;
    }
    public void setFluxRuntimeUrl(String fluxRuntimeUrl) {
        this.fluxRuntimeUrl = fluxRuntimeUrl;
    }
    public long getSocketTimeout() {
        return socketTimeout;
    }
    public void setSocketTimeout(long socketTimeout) {
        this.socketTimeout = socketTimeout;
    }
    public long getConnectionTimeout() {
        return connectionTimeout;
    }
    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
