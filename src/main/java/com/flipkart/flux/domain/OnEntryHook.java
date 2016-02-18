package com.flipkart.flux.domain;

/**
 * @understands Represents a hook whose execution will be started asynchronously immediately on a state entry
 * Main task execution won't be impacted or waited by hook execution.
 */
public class OnEntryHook implements Hook {

    @Override
    public void execute() {

    }
}