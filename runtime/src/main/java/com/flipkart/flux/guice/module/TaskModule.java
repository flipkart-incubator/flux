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

package com.flipkart.flux.guice.module;

import com.flipkart.flux.Constants;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.guice.annotation.ManagedEnv;
import com.flipkart.flux.impl.eventscheduler.EventSchedulerRegistryImpl;
import com.flipkart.flux.impl.redriver.RedriverRegistryImpl;
import com.flipkart.flux.impl.task.AkkaTask;
import com.flipkart.flux.impl.task.registry.EagerInitRouterRegistryImpl;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.flux.initializer.FluxInitializer;
import com.flipkart.flux.module.SchedulerModule;
import com.flipkart.flux.registry.TaskExecutableRegistryImpl;
import com.flipkart.flux.task.eventscheduler.EventSchedulerRegistry;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.flipkart.flux.taskDispatcher.TaskDispatcher;
import com.flipkart.flux.taskDispatcher.TaskDispatcherImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Guice module for the Task Runtime
 *
 * @author yogesh.nachnani
 * @author shyam.akirala
 */
public class TaskModule extends AbstractModule {

    public TaskModule() {
    }

    @Override
    protected void configure() {
        if (FluxInitializer.role.equals(Constants.ORCHESTRATION)) {
            bind(TaskDispatcher.class).to(TaskDispatcherImpl.class).in(Singleton.class);
            bind(RedriverRegistry.class).to(RedriverRegistryImpl.class);
            bind(EventSchedulerRegistry.class).to(EventSchedulerRegistryImpl.class);
            install(new SchedulerModule());
        } else {
            bind(RouterRegistry.class).to(EagerInitRouterRegistryImpl.class);
            bind(ExecutableRegistry.class).annotatedWith(ManagedEnv.class).to(TaskExecutableRegistryImpl.class);
            requestStaticInjection(AkkaTask.class);
        }
        install(new FluxClientComponentModule());
    }
}
