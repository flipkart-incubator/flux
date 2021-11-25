package com.flipkart.flux.exception;

/***
 *   <code> ReplayEventException</code> indicates that the retry count for a replayable state have been exhausted.
 *
 * @author raghav
 */
public class ReplayEventException extends RuntimeException {

    public ReplayEventException(String message) {
        super(message);
    }

}
