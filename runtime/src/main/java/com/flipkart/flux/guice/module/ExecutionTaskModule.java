package com.flipkart.flux.guice.module;

import com.flipkart.flux.annotation.ManagedEnv;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.impl.task.AkkaTask;
import com.flipkart.flux.impl.task.registry.EagerInitRouterRegistryImpl;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.flux.registry.TaskExecutableRegistryImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class ExecutionTaskModule extends AbstractModule {

    public ExecutionTaskModule() {
    }

    @Override
    protected void configure() {
        bind(RouterRegistry.class).to(EagerInitRouterRegistryImpl.class).in(Singleton.class);
        bind(ExecutableRegistry.class).annotatedWith(ManagedEnv.class).to(TaskExecutableRegistryImpl.class);
        requestStaticInjection(AkkaTask.class);
    }
}
