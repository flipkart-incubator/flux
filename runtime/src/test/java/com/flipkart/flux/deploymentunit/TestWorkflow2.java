package com.flipkart.flux.deploymentunit;

import com.flipkart.flux.client.model.Task;

/**
 * @author gaurav.ashok
 */
public class TestWorkflow2 {

    public static int testTask1Counter = 0;

    @Task(version = 1L, timeout = 1000l)
    public void testTask() {
        System.out.println("====testTask execution====");
        testTask1Counter++;
    }
}
