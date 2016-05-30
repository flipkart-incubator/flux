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

package com.flipkart.flux.integration;

import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * Workflow used in <code>WorkflowInterceptorTest</code> to test e2e interception
 */
@Singleton
public class SimpleWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWorkflow.class);

    /* A simple workflow that goes about creating tasks and making merry */
    @Workflow(version = 1)
    public void simpleDummyWorkflow() {
        final String newString = simpleStringReturningTask();
        final Integer someNewInteger = simpleIntegerReturningTask();
        someTaskWithIntegerAndString(newString, someNewInteger);
    }

    @Task(version = 2,retries = 2,timeout = 2000l)
    public String simpleStringReturningTask() {
        logger.info("In Simple String returning task");
        return "randomBs";
    }

    @Task(version = 1, retries = 2, timeout = 3000l)
    public Integer simpleIntegerReturningTask() {
        logger.info("In Simple Integer returning task");
        return 2;
    }

    @Task(version = 3, retries = 0, timeout = 1000l)
    public void someTaskWithIntegerAndString(String someString, Integer someInteger) {
        //blah
        logger.info("In some task with integer and string");
    }

}
