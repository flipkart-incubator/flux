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
import java.util.concurrent.CountDownLatch;

/**
 * Workflow used in <code>WorkflowInterceptorTest</code> to test e2e interception
 */
@Singleton
public class SimpleWorkflow {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWorkflow.class);
    private CountDownLatch countDownLatchForSimpleStringReturnTask = new CountDownLatch(1);
    private CountDownLatch countDownLatchForSimpleIntegerReturnTask = new CountDownLatch(1);
    private CountDownLatch countDownLatchForSimpleIntegerAndStringTask = new CountDownLatch(1);

    /* A simple workflow that goes about creating tasks and making merry */
    @Workflow(version = 1)
    public void simpleDummyWorkflow(StringEvent startingEvent) {
        final StringEvent newString = simpleStringReturningTask(startingEvent);
        final IntegerEvent someNewInteger = simpleIntegerReturningTask();
        someTaskWithIntegerAndString(newString, someNewInteger);
    }

    @Task(version = 2,retries = 2,timeout = 200000l)
    public StringEvent simpleStringReturningTask(StringEvent stringEvent) {
        logger.info("In Simple String returning task {}, received",stringEvent);
        countDownLatchForSimpleStringReturnTask.countDown();
        return new StringEvent("randomString");
    }

    @Task(version = 1, retries = 2, timeout = 3000l)
    public IntegerEvent simpleIntegerReturningTask() {
        logger.info("In Simple Integer returning task");
        countDownLatchForSimpleIntegerReturnTask.countDown();
        return new IntegerEvent(2);
    }

    @Task(version = 3, retries = 0, timeout = 1000l)
    public void someTaskWithIntegerAndString(StringEvent someString, IntegerEvent someInteger) {
        logger.info("In someTaskWithIntegerAndString with integer {} and string {}",someInteger,someString);
        countDownLatchForSimpleIntegerAndStringTask.countDown();
    }

    public CountDownLatch getCountDownLatchForSimpleIntegerReturnTask() {
        return countDownLatchForSimpleIntegerReturnTask;
    }

    public CountDownLatch getCountDownLatchForSimpleStringReturnTask() {
        return countDownLatchForSimpleStringReturnTask;
    }

    public CountDownLatch getCountDownLatchForSimpleIntegerAndStringTask() {
        return countDownLatchForSimpleIntegerAndStringTask;
    }
}
