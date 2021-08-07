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

import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;

/**
 * Situation: The workflow involves a manual process. Upon completing the manual process, we can post the requisite data
 * back to the flux engine and the workflow continues
 * Things to Note:
 * 1. The workflow essentially pauses at sellerVerificationService.waitForVerification call. Notice that the passed parameter is null
 * 2. Observe the state of the workflow in the admin dashboard (default at http://localhost:9999/admin/fsmview)
 * 3. Try posting the requisite data to flux and see it continue- // TODO (provide an easy way to do it)
 * 4. Observe the state of the workflow in the admin dashboard again
 * 5. Notice the implementation of sellerVerificationService.waitForVerificationService(). It re-sends the same sellerVerificationStatus.
 * The SellerVerificationStatus parameter passed to the method and the one returned by the method are treated as different events by the runtime
 */
public class ManualSellerVerificationFlow {
    @Inject
    private SellerDataService sellerDataService;

    @Inject
    private ManualSellerVerificationService sellerVerificationService;

    @Inject
    NotificationService notificationService;

    @Workflow(version = 1)
    public void verifySeller(SellerId sellerId) {
        /* Retrieve SellerData using the given seller id */
        SellerData sellerData = sellerDataService.retrieveSellerData(sellerId);
        /* Proceed the workflow and notify customer support of the task at hand */
        notificationService.notifyCustomerSupport(sellerData);
        /* The workflow will not proceed to invoke this event untill and unless the requisite data is POSTed back to the flux engine */
        final SellerVerificationStatus sellerVerificationStatus = sellerVerificationService.waitForVerification(null);
        confirmVerificationStatus(sellerVerificationStatus);
    }

    @Task(version = 1, timeout = 1000l, retries = 2)
    public void confirmVerificationStatus(SellerVerificationStatus sellerVerificationStatus) {
        if (sellerVerificationStatus.isVerifiedSeller()) {
            notificationService.sendWelcomeEmail(sellerVerificationStatus.getSellerId());
        }
    }
}
