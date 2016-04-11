package com.flipkart.flux.examples.concurrent;

import com.flipkart.flux.client.model.Task;

import java.util.Random;

/**
 * Dummy Implementation of a class that implements all the 4 tasks needed to execute <code>SimpleConcurrentWorkflow</code>
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
