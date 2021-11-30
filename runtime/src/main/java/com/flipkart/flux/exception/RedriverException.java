package com.flipkart.flux.exception;

/**
 * <code>RedriverException</code> is thrown when a state cannot redrive.
 * @author raghav
 */
public class RedriverException extends RuntimeException {
    public RedriverException(String message) {
        super(message);
    }
}
