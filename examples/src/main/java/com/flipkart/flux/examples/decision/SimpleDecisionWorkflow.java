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

import com.flipkart.flux.client.model.Promise;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;

/**
 * Situation : The workflow needs to react differently based on the output of one of the task
 */
public class SimpleDecisionWorkflow {

    @Inject
    private SimpleTaskExecutor simpleTaskExecutor;

    @Inject
    private NotificationService notificationService;

    @Workflow(version = 1)
    public void startWorkflow() {
        final Promise<TaskData> taskDataPromise = simpleTaskExecutor.performSimpleTask();
        notifyBadData(taskDataPromise);
    }

    @Task(version = 1, timeout = 1000l)
    public void notifyBadData(Promise<TaskData> taskDataPromise) {
        if (taskDataPromise.get().isGood()) {
            System.out.print("All Izz Well");
        } else {
            notificationService.notify(taskDataPromise.get().getId());
        }
    }


}
