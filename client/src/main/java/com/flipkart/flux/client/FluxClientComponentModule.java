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

package com.flipkart.flux.client;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.client.config.FluxClientConfiguration;
import com.flipkart.flux.client.guice.annotation.IsolatedEnv;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.client.registry.LocalExecutableRegistryImpl;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.FluxRuntimeConnectorHttpImpl;
import com.flipkart.flux.client.runtime.LocalContext;

import javax.inject.Singleton;

/**
 * <code>FluxClientComponentModule</code> is a Guice {@link AbstractModule} implementation which
 * wires and provides classes to support task execution.
 *
 * <p> Ensure that you have a provider for the FluxClientConfiguration in one of your application's
 * guice Module. Example:
 * <pre><code>
 * {@literal @}Produces
 * {@literal @}Singleton
 *  FluxClientConfiguration providesFluxClientConfiguration(AppConfiguration configuration) {
 *    return configuration.getFluxClientConfiguration();
 *  }
 * </code>
 * </pre>
 * </p>
 *
 * @author yogesh.nachnani
 * @author shyam.akirala
 */
public class FluxClientComponentModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ExecutableRegistry.class).annotatedWith(IsolatedEnv.class).to(LocalExecutableRegistryImpl.class);
    }

    @Provides
    @Singleton
    public FluxRuntimeConnector provideFluxRuntimeConnector(FluxClientConfiguration configuration,
                                                            ObjectMapper objectMapper) {
        String fluxRuntimeUrl = System.getProperty("flux.runtimeUrl");
        if(fluxRuntimeUrl == null) {
            fluxRuntimeUrl = configuration.getFluxRuntimeUrl();
        }
        return new FluxRuntimeConnectorHttpImpl(configuration.getConnectionTimeout(),
                                                configuration.getSocketTimeout(),
                                                fluxRuntimeUrl + "/api/machines",
                                                objectMapper);
    }

    @Provides
    @Singleton
    public LocalContext getLocalContext() {
        return new LocalContext();
    }
}
