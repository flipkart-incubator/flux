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

package com.flipkart.flux.integration;

import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RunSimpleWorkflow {

	public static void main(String... args) throws Exception {

        /* Initialise _your_ module*/
        final Injector injector = Guice.createInjector(new FluxClientComponentModule(), new FluxClientInterceptorModule());

        /* Note that we are using guice aop for now, hence your workflow instances need to use guice */
        final SimpleWorkflow simpleWorkflow = injector.getInstance(SimpleWorkflow.class);
        /* Lets invoke our workflow */
        System.out.println("[Main] Starting workflow execution");
        simpleWorkflow.simpleDummyWorkflow((new StringEvent("startingEvent")));
        System.out.println("[Main] See flux dashboard at http://localhost:9999/admin/dashboard");
        /* Observe the logs and see how different emails are sent from different threads! Its magic! */
    }
}