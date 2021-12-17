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
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RunManualSellerVerificationWorkflow {
    public static void main(String... args) throws Exception {

        /* Initialise _your_ module*/
        final Injector injector = Guice.createInjector(new FluxClientComponentModule(), new FluxClientInterceptorModule());

        /* Note that we are using guice aop for now, hence your workflow instances need to use guice */
        final ManualSellerVerificationFlow manualSellerVerificationFlow = injector.getInstance(ManualSellerVerificationFlow.class);
        /* Lets invoke our workflow */

        String randomCorrelationId = "my-correlation-id-23";
        manualSellerVerificationFlow.verifySeller(new SellerId(1l, randomCorrelationId));

        /* Since we've initialised flux, the process will continue to run till you explicitly kill it */
        /* The workflow is currently waiting for the seller to post  the external event*/
        System.out.println("[Main] The workflow is active. Try checking in the UI first & then post an external event against " + randomCorrelationId);

        /**
         * The workflow will not terminate till you have posted a manual event. A sample json for a manual event is :
         {
         "name":"sellerVerification",
         "type":"com.flipkart.flux.examples.externalevents.SellerVerificationStatus",
         "data": "{\"sellerId\": {\"id\" : 1},\"verified\":true}",
         "eventSource" : "yogesh_nachnani"
         }
         */

        /* You may also choose to uncomment the following code to post an external event */

        System.out.println("[Main] Sleeping for 2 seconds before posting data to flux runtime");
        Thread.sleep(2000l); // Just a 2 second wait to ensure that the state machine has been created in flux
        injector.getInstance(FluxRuntimeConnector.class).submitEvent("sellerVerification", new SellerVerificationStatus(new SellerId(1l), true), randomCorrelationId, "Manual Trigger From Customer Support");
        System.out.println("[Main] Posted data to flux runtime, the workflow should have continued");
        System.out.println("[Main] Visit flux dashboard at http://localhost:9999/admin/fsmview and " +
                "enter " + randomCorrelationId + " to see workflow execution details");
    }
}