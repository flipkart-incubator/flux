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

package com.flipkart.flux.examples.perf;

import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;

/**
 * @author shyam.akirala
 */
public class PerfWorkflow {

    @Workflow(version = 0)
    public void perfWorkflow() {
        FluxPackage one = task1();
        FluxPackage two = task2();
        FluxPackage three = task3();
        FluxPackage four = task4();
        task5(one, two, three, four);
    }

    @Task(version = 0, timeout = 5000, retries = 2)
    public FluxPackage task1() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new FluxPackage("one");
    }

    @Task(version = 0, timeout = 5000, retries = 2)
    public FluxPackage task2() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new FluxPackage("two");
    }

    @Task(version = 0, timeout = 5000, retries = 2)
    public FluxPackage task3() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new FluxPackage("three");
    }

    @Task(version = 0, timeout = 5000, retries = 2)
    public FluxPackage task4() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new FluxPackage("four");
    }

    @Task(version = 0, timeout = 5000, retries = 2)
    public void task5(FluxPackage one, FluxPackage two, FluxPackage three, FluxPackage four) {

    }
}
