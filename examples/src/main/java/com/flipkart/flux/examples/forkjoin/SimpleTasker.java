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

package com.flipkart.flux.examples.forkjoin;

import com.flipkart.flux.client.model.Task;

import java.util.List;

/**
 * Dummy class that implements methods needed by <code>SimpleForkJoinWorkflow</code>
 */
public class SimpleTasker {

    /**
     * Concatenates the given listOfCharacters into one message and stores it in db
     * @param listOfCharacters
     */
    @Task(version = 1, retries = 2, timeout = 1000l)
    public void join(List<Character> listOfCharacters) {
        StringBuffer reCreation = new StringBuffer();
        listOfCharacters.forEach(reCreation::append);
        // Write the reCreated string in DB
    }

    /**
     * This is nothing but a fancy way doing inputString.charAt(index)
     * We can afford to have multiple retries since this is an idempotent operation
     * @param inputString
     * @param index
     * @return the character at given index in the inputString
     */
    @Task(version = 1, retries = 2, timeout = 1000l)
    public Character fork(String inputString, int index) {
        return inputString.charAt(index);
    }
}
