package com.flipkart.flux.exception;

/**
 * <code>TraversalPathException</code> used to denote that no traversal path found.
 * @author akif.khan
 */
public class TraversalPathException extends RuntimeException {
    public TraversalPathException(String message) {
        super(message);
    }
}
