package com.flipkart.flux.client.registry;

public interface Executable {

    String getName();
    long getTimeout();
    Object execute(Object[] parameters);
}
