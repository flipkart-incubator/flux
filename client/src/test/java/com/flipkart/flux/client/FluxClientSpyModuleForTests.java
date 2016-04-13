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

package com.flipkart.flux.client;

import com.flipkart.flux.client.intercept.SimpleWorkflowForTest;
import com.flipkart.flux.client.intercept.WorkflowInterceptor;
import com.flipkart.flux.client.model.Workflow;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.LocalContext;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import org.mockito.Mockito;

import javax.inject.Singleton;

public class FluxClientSpyModuleForTests extends AbstractModule {

    @Override
    protected void configure() {
        bind(SimpleWorkflowForTest.class);
        final WorkflowInterceptor workflowInterceptor = new WorkflowInterceptor();
        requestInjection(workflowInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Workflow.class),
            workflowInterceptor);
    }

    @Provides
    @Singleton
    public LocalContext provideLocalContext( ) {
        return Mockito.spy(new LocalContext());
    }

    @Provides
    @Singleton
    public FluxRuntimeConnector provideFluxRuntimeConnector( ){
        return Mockito.spy(new FluxRuntimeConnector());
    }
}
