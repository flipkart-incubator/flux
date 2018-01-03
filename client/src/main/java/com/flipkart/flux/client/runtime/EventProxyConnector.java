package com.flipkart.flux.client.runtime;

import com.flipkart.flux.client.config.FluxClientConfiguration;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EventProxyConnector extends FluxRuntimeConnectorHttpImpl {

    public static Logger logger = LoggerFactory.getLogger(EventProxyConnector.class);

    @Inject
    public EventProxyConnector(@Named("eventProxyForMigration.endpoint") String endpoint, FluxClientConfiguration fluxClientConfiguration) {
        super(fluxClientConfiguration.getConnectionTimeout(), fluxClientConfiguration.getSocketTimeout(),
                endpoint);
    }

}


















