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

package com.flipkart.flux.examples.externalevents;

import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.client.runtime.FluxRuntimeConnectorHttpImpl;
import com.flipkart.flux.initializer.FluxInitializer;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.UUID;

public class RunManualUserVerificationWorkflow {
    public static void main(String... args) throws Exception {
        /* Bring up the flux runtime */
        FluxInitializer.main(new String[]{});

        /* Initialise _your_ module*/
        final Injector injector = Guice.createInjector(new FluxClientComponentModule(), new FluxClientInterceptorModule());

        /* Note that we are using guice aop for now, hence your workflow instances need to use guice */
        final ManualUserVerificationFlow manualUserVerificationFlow = injector.getInstance(ManualUserVerificationFlow.class);
        /* Lets invoke our workflow */

        String randomCorrelationId = UUID.randomUUID().toString().substring(0,16);
        manualUserVerificationFlow.verifyUser(new UserId(1l, randomCorrelationId));

        /* Since we've initialised flux, the process will continue to run till you explicitly kill it */
        /* The workflow is currently waiting for the user to post  the external event*/
        System.out.println("[Main] The workflow is active. Try checking in the UI first & then post an external event against " + randomCorrelationId);

        /**
         * The workflow will not terminate till you have posted a manual event. A sample json for a manual event is :
          {
           "name":"userVerification",
           "type":"com.flipkart.flux.examples.externalevents.UserVerificationStatus",
           "data": "{\"userId\": {\"id\" : 1},\"verified\":true}",
           "eventSource" : "yogesh_nachnani"
         }
         */

        /* You may also choose to uncomment the following code to post an external event */

/*
        System.out.println("[Main] Sleeping for 2 seconds before posting data to flux runtime");
        Thread.sleep(2000l); // Just a 2 second wait to ensure that the state machine has been created in flux
        new FluxRuntimeConnectorHttpImpl(1000l,1000l,"http://localhost:9998/api/machines").submitEvent("userVerification", new UserVerificationStatus(new UserId(1l), true), randomCorrelationId, "Main Method");
        System.out.println("[Main] Posted data to flux runtime, the workflow should have continued");
*/

    }
}
