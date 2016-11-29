package com.flipkart.flux.deploymentunit;

import com.flipkart.flux.client.runtime.Stoppable;

/**
 * @author gaurav.ashok
 */
public class StoppableImpl implements Stoppable {
    public static Integer stopCounter = 0;
    public void stop() {
        System.out.println("Stopping");
        stopCounter ++;
    }
}