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

import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * A workflow designed to send emails to a given set of recepients
 * To speed up execution, we send the email out
 * Things to Note
 * 1. Observe how concurrency works - there is no method that depends on output of another method
 * 2. Observe how the methods EmailMarketingWorkflow.sendEmails and EmailDispatcher.sendEmail are annotated with @Task
 * 3. Observe how we are using javax.inject annotations for Dependency Injection
 */
@Singleton
public class EmailMarketingWorkflow {

    @Inject
    private EmailDispatcher emailDispatcher;

    @Workflow(version = 1)
    public void sendEmails(Email...emails) {
        /** Each call to emailDispatcher.sendMail() is executed concurrently and possibly on different hosts */
        for (Email email : emails) {
            emailDispatcher.sendEmail(email);
        }

    }
}
