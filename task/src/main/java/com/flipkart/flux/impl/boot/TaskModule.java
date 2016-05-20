package com.flipkart.flux.impl.boot;

import com.flipkart.flux.impl.task.registry.EagerInitRouterRegistryImpl;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.flux.impl.task.registry.LocalRouterConfigurationRegistryImpl;
import com.flipkart.flux.impl.task.registry.RouterConfigurationRegistry;
import com.flipkart.polyguice.core.ConfigurationProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Guice module for the Task Runtime
 * @author yogesh.nachnani
 */
public class TaskModule extends AbstractModule {

    private ConfigurationProvider configurationProvider;

    public TaskModule(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    protected void configure() {
        bind(RouterConfigurationRegistry.class).to(LocalRouterConfigurationRegistryImpl.class);
        bind(RouterRegistry.class).to(EagerInitRouterRegistryImpl.class);
    }

    @Provides
    @Singleton
    public ConfigurationProvider configurationProvider() {
        return this.configurationProvider;
    }

    /* Following are hacks that need to go away soon */
    @Provides
    @Singleton
    @Named("router.names")
    public Set<String> getRouterNames() {
        // TODO - this needs to be Provided by the boot util that loads all deployment units
        return new HashSet<String>(){{add("someRouter");add("someRouterWithoutConfig");}};
    }

    @Named("runtime.actorsystem.metrics")
    @Provides
    public Boolean getWithMetricsConf() {
        return false;
    }

    @Named("runtime.actorsystem.name")
    @Provides
    public String getActorSystemName() {
        return "FluxSystem";
    }

    @Named("runtime.actorsystem.configname")
    @Provides
    public String getActorConfigFileName() {
        return "application.conf";
    }
}
