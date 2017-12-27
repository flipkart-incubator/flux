package com.flipkart.flux.guice.module;

import com.flipkart.flux.client.config.FluxClientConfiguration;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.FluxRuntimeConnectorHttpImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class EventProxyModule extends AbstractModule {

    @Override
    public void configure() {

    }

    @Named("EventProxyClient")
    @Singleton
    @Provides
    public FluxRuntimeConnector getEventProxyRuntimeConnector(@Named("eventProxyForMigration.enabled") String enabled,
                                                              @Named("eventProxyForMigration.endpoint") String oldEndPoint,
                                                              FluxClientConfiguration fluxClientConfiguration) {
        if (!enabled.isEmpty() && enabled.equalsIgnoreCase("yes")
                && !oldEndPoint.isEmpty()) {
            return new FluxRuntimeConnectorHttpImpl(fluxClientConfiguration.getConnectionTimeout(),
                    fluxClientConfiguration.getSocketTimeout(),
                    oldEndPoint + "/api/machines");
        }
        return null;
    }
}


















