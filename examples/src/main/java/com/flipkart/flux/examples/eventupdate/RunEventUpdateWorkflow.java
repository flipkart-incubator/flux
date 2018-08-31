/*
 * Copyright 2012-2018, the original author or authors.
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

package com.flipkart.flux.examples.eventupdate;

import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class can be used to run and observe the <code>EventUpdateWorkflow</code>
 * This or a similar class like this is _not_ required to be present in your actual production jar
 *
 * Created by akif.khan on 31/08/18.
 */
public class RunEventUpdateWorkflow {
    public static void main(String[] args) {

        /* Initialise _your_ module*/
        final Injector injector = Guice.createInjector(new FluxClientComponentModule(), new FluxClientInterceptorModule());

        /* Note that we are using guice aop for now, hence your workflow instances need to use guice */
        EventUpdateWorkflow eventUpdateWorkflow = injector.getInstance(EventUpdateWorkflow.class);

        /* Lets invoke our workflow */
        System.out.println("[Main] Starting workflow execution");
        eventUpdateWorkflow.create(new StartEvent("example_event_update_workflow"));

        /* Since we've initialised flux, the process will continue to run till you explicitly kill it */
    }
}
