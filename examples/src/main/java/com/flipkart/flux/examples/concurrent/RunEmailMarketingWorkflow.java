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

package com.flipkart.flux.examples.concurrent;

import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.initializer.FluxInitializer;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class can be used to run and observe the email marketing workflow.
 * This or a similar class like this is _not_ required to be present in your actual production jar
 */
public class RunEmailMarketingWorkflow  {
    public static void main(String... args) throws Exception {

        /* Bring up the flux runtime */
        FluxInitializer.main(new String[]{});

        /* Initialise _your_ module*/
        final Injector injector = Guice.createInjector(new FluxClientComponentModule(), new FluxClientInterceptorModule());

        /* Note that we are using guice aop for now, hence your workflow instances need to use guice */
        final EmailMarketingWorkflow emailMarketingWorkflow = injector.getInstance(EmailMarketingWorkflow.class);
        /* Lets invoke our workflow */
        emailMarketingWorkflow.sendEmails(new Email("someBody","someone@flipkart.com"),new Email("someMore","someoneElse@fk.com"));
        /* Observe the logs and see how different emails are sent from different threads! Its magic! */
    }
}
