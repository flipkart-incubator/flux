package com.flipkart.flux.examples.concurrent;

import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;

/**
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
