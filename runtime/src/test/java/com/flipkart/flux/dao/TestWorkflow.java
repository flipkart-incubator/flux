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

package com.flipkart.flux.dao;

import com.flipkart.flux.client.exception.FluxRetriableException;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.integration.IntegerEvent;

/**
 * @author shyam.akirala
 */
public class TestWorkflow {

    public static boolean shouldFail = false;

    @Task(version = 1L, timeout = 1000l)
    public String testTask() {
        System.out.println("====testTask execution====");
        return "testEvent";
    }

    @Task(version = 1L, timeout = 1000l)
    public String testTask(String input) {
        System.out.println("====testTask execution for String: " + input + "====");
        return "testTask(" + input + ")";
    }

    @Task(version = 1L, timeout = 1000l)
    public void testTask(String input, Integer i) {
        System.out.println("====testTask execution for String: " + input + i + "====");
    }

    @Task(version = 1L, timeout = 400l, retries = 1)
    public Integer dummyTask(String integer) {
        System.out.println("====dummyTask execution====: " + integer);
        if(shouldFail) {
            try {
                Thread.sleep(1000);
            } catch (Exception e){
                System.out.println("interrupted");
            }
        }
        try {
            return Integer.parseInt(integer);
        } catch (Exception e) {

        }
        return 0;
    }
}
