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
 *
 */

package com.flipkart.flux.examples.concurrent;

import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;

/**
 * Situation: We have an opportunity to speed up execution by executing independent tasks concurrently
 * Example workflow to demonstrate how concurrent workflows can be written
 * @author yogesh.nachnani
 */
public class SimpleConcurrentWorkflow {

    @Inject
    SimpleTasker simpleTasker;

    @Workflow(version = 1)
    public void begin() {
        /* Task A will be the first to be executed since it does not have any dependencies */
        final String randomString = simpleTasker.taskA();

        /*
           Task B & Task C can be executed only once Task A is completed.
           This is specified by adding the output of taskA as inputs to tasks B & C
         */
        final Integer length = simpleTasker.taskB(randomString);
        final Boolean isPalindrome = simpleTasker.taskC(randomString);

        /*Task D can be executed only once Task B & C are completed */
        simpleTasker.taskD(length,isPalindrome);
    }
}
