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
 */

package com.flipkart.flux.client;

import com.flipkart.flux.client.intercept.TaskInterceptor;
import com.flipkart.flux.client.intercept.WorkflowInterceptor;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * <code>FluxClientInterceptorModule</code> is a Guice {@link AbstractModule} implementation
 * used for wiring workflow interceptor classes.
 * @author yogesh.nachnani
 */
public class FluxClientInterceptorModule extends AbstractModule {
    @Override
    protected void configure() {
        final WorkflowInterceptor workflowInterceptor = new WorkflowInterceptor();
        requestInjection(workflowInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Workflow.class),
            workflowInterceptor);
        final TaskInterceptor taskInterceptor = new TaskInterceptor();
        requestInjection(taskInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Task.class), taskInterceptor);
    }
}
