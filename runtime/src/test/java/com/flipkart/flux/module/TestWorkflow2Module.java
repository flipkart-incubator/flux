package com.flipkart.flux.module;

import com.flipkart.flux.client.runtime.Stoppable;
import com.flipkart.flux.deploymentunit.StoppableImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author gaurav.ashok
 */
public class TestWorkflow2Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(Stoppable.class).to(StoppableImpl.class).in(Singleton.class);
    }
}