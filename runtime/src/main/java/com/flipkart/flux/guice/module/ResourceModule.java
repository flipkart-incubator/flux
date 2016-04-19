package com.flipkart.flux.guice.module;

import com.flipkart.flux.resource.FluxUIResource;
import com.flipkart.flux.resource.StateMachineResource;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.glassfish.jersey.server.ResourceConfig;

public class ResourceModule extends AbstractModule {



    protected void configure() {

    }

    @Singleton
    @Provides
    @Named("APIResourceConfig")
    public ResourceConfig getAPIResourceConfig() {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(FluxUIResource.class);
        resourceConfig.register(StateMachineResource.class);
        return resourceConfig;
    }

    @Singleton
    @Provides
    @Named("AdminResourceConfig")
    public ResourceConfig getAdminResourceConfig() {
        ResourceConfig resourceConfig = new ResourceConfig();
        return resourceConfig;
    }


}
