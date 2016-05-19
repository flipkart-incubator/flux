package com.flipkart.flux.impl.temp;

import java.util.concurrent.TimeUnit;

public class Time {

    private long start;
    private long end;

    public void start() {
        this.start = System.nanoTime();
    }

    public void end() {
        this.end = System.nanoTime();
    }

    public long elapsedTimeMilliseconds() {
        return TimeUnit.MILLISECONDS.convert((end - start), TimeUnit.NANOSECONDS);
    }
}