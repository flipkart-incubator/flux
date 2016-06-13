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

package com.flipkart.flux.examples.decision;

import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Situation : The workflow needs to react differently based on the output of one of the task
 * Example: If the user is verified, we send her a welcome email; Else we notify customer support
 * Things to Note:
 * 1. Observe how the flow is created : retrieveUserData -> verifyUser -> checkVerificationStatus using output parameters
 * of one method as input parameters to the other
 * 2. Observe how a decision is taken within a task.
 * 3. Going forward, all task invocations (from either a <code>Workflow</code> or another <code>Task</code>) will
 * be executed remotely. Presently <code>Task</code> invocations from within a <code>Task</code> are executed on the same thread.
 *
 */
@Singleton
public class UserVerificationWorkflow {

    @Inject
    private UserVerificationService userVerificationService;

    @Inject
    private NotificationService notificationService;

    @Inject
    private UserDataService userDataService;

    @Workflow(version = 1)
    public void verifyUser(UserId userId) {

        /* Retrieve UserData using the given user id */
        UserData userData = userDataService.retrieveUserData(userId);

        /* Use the verification service to verify user based on given data */
        final UserVerificationStatus userVerificationStatus = userVerificationService.verifyUser(userData);

        /*
          Determining if the User is verified or not can only happen after we have the UserVerificationStatus
          We cannot write a simple if statement in this method itself.
          This is because, in a Flux production environment, the verifyUser(UserId userId) method simply creates the workflow in the Flux engine
          The actual execution and orchestration is handled by Flux.
          The real verificationStatus is made available to the below function once
          userVerificationService.verifyUser() above is successfully completed
        */
        checkVerificationStatus(userVerificationStatus);
    }

    /**
     * Idempotent operation that would use the notificationService to notifyCustomerSupport about non-verified users
     * OR would send out a welcome email if the user is verified
     * @param userVerificationStatus
     */
    @Task(version = 1, timeout = 1000l, retries = 2)
    public void checkVerificationStatus(UserVerificationStatus userVerificationStatus) {
        if (userVerificationStatus.isVerifiedUser()) {
            notificationService.sendWelcomeEmail(userVerificationStatus.getUserId());
        } else {
            notificationService.notifyCustomerSupport(userVerificationStatus.getUserId());
        }
    }
}
