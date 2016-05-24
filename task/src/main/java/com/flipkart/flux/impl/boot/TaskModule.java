package com.flipkart.flux.impl.boot;

import com.flipkart.flux.impl.task.CustomSuperviseStrategy;
import com.flipkart.flux.impl.task.registry.EagerInitRouterRegistryImpl;
import com.flipkart.flux.impl.task.registry.LocalRouterConfigurationRegistryImpl;
import com.flipkart.flux.impl.task.registry.RouterConfigurationRegistry;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Guice module for the Task Runtime
 * @author yogesh.nachnani
 */
public class TaskModule extends AbstractModule {


    public TaskModule() {
    }

    @Override
    protected void configure() {
        bind(RouterConfigurationRegistry.class).to(LocalRouterConfigurationRegistryImpl.class);
        bind(RouterRegistry.class).to(EagerInitRouterRegistryImpl.class);
    }

    /* Following are hacks that need to go away soon */
    @Provides
    @Singleton
    @Named("router.names")
    public Set<String> getRouterNames() {
        // TODO - this needs to be Provided by the boot util that loads all deployment units
        return new HashSet<String>(){{add("someRouter");add("someRouterWithoutConfig");}};
    }

    @Provides
    @Singleton
    public CustomSuperviseStrategy getSupervisorStrategy() {
        //todo re-tries to come from config
        return new CustomSuperviseStrategy(2);
    }
}
