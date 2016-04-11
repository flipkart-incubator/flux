package com.flipkart.flux.examples.concurrent;

import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;

public class SimpleConcurrentWorkflow {

    @Inject
    SimpleTasker simpleTasker;

    @Workflow(version = 1)
    public void begin() {
        final String randomString = simpleTasker.taskA();
        final Integer length = simpleTasker.taskB(randomString);
        final Boolean isPalindrome = simpleTasker.taskC(randomString);
        simpleTasker.taskD(length,isPalindrome);
    }
}
