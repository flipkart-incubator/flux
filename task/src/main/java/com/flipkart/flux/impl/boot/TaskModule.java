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
 *
 */

package com.flipkart.flux.impl.boot;

import com.flipkart.flux.impl.task.AkkaTask;
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
        requestStaticInjection(AkkaTask.class);
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
