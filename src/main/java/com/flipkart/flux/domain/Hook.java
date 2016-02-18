package com.flipkart.flux.domain;

/**
 * @describes Hook can be executed asynchronously on entry or exit of a state
 */
public interface Hook {

    public void execute();

}
