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

import com.flipkart.flux.client.model.Task;

import java.util.Random;

/**
 * Dummy Implementation of a class that implements all the 4 tasks needed to execute <code>SimpleConcurrentWorkflow</code>
 * Note that each of the Task methods are re-entrant do not make use of any internal state of the class.
 * Each of these methods is capable of being executed independently on any machine based purely on their input requirements.
 * @author yogesh.nachnani
 */
public class SimpleTasker {

    /* Generates a random string */
    @Task(version = 1,retries = 0, timeout = 1000l)
    public String taskA() {
        return "This is a simple string which is not truly random. The user thinks its random, but its not. " +
            "This is encapsulation at its finest";
    }

    /* Calculates the length of the string */
    @Task(version = 1,retries = 0, timeout = 1000l)
    public Integer taskB(String input) {
        return input.length();
    }

    /* Finds out if a given string is a palindrome or not */
    @Task(version = 1,retries = 0, timeout = 1000l)
    public Boolean taskC(String input) {
        return isPalindrome(input);
    }

    @Task(version = 1,retries = 0, timeout = 1000l)
    public void taskD(Integer length, Boolean isPalindrome) {
        String msg = "The String is " + length +" characters long and " + (isPalindrome ? "is" : "is not") + " a palindrome";
        // Send message - store in DB, call an HTTP endpoint - upto you
    }

    private Boolean isPalindrome(String input) {
        /* Not a very inspiring implementation */
        return new Random(System.currentTimeMillis()).nextBoolean();
    }

}
