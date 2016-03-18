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

package com.flipkart.flux.examples.di;

import com.flipkart.flux.client.model.Promise;
import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;

/**
 * This workflow demonstrates the use of Dynamically injected components within a Workflow
 *
 * Situation: We need to store the message received in the database and then notify another customer via HTTP
 *
 */
public class SimpleStoreAndNotifyWorkflow {
    /**
     * Flux is capable of performing DI within any class that hosts its @Task or @Workflow annotations
     * <b> We support javax.inject annotations only </b>
     */
    @Inject
    private StorageService storageService;

    @Inject
    private NotificationService notificationService;

    @Workflow(version = 1)
    public void receiveMessage(Message message) {

        /**
         * This would be non trivial to achieve if it were a regular non-flux driven code.
         * There could be a notification loss after persisting to DB in case of a JVM crash.
         * In case of flux, however, the message will be reliably sent to the notification call
         * (executed probably by another JVM)
         */
        final Promise<Long> messageId = storageService.store(message);
        notificationService.notify(messageId);
    }
}
