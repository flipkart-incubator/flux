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

import com.flipkart.flux.impl.eventscheduler.EventSchedulerRegistryImpl;
import com.flipkart.flux.impl.redriver.RedriverRegistryImpl;
import com.flipkart.flux.module.SchedulerModule;
import com.flipkart.flux.task.eventscheduler.EventSchedulerRegistry;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.flipkart.flux.taskDispatcher.ExecutionNodeTaskDispatcher;
import com.flipkart.flux.taskDispatcher.ExecutionNodeTaskDispatcherImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Guice module for the Task Runtime
 *
 * @author yogesh.nachnani
 * @author shyam.akirala
 */
public class OrchestrationTaskModule extends AbstractModule {

    public OrchestrationTaskModule() {
    }

    @Override
    protected void configure() {
        bind(ExecutionNodeTaskDispatcher.class).to(ExecutionNodeTaskDispatcherImpl.class).in(Singleton.class);
        bind(RedriverRegistry.class).to(RedriverRegistryImpl.class);
        bind(EventSchedulerRegistry.class).to(EventSchedulerRegistryImpl.class);
        install(new SchedulerModule());
    }
}