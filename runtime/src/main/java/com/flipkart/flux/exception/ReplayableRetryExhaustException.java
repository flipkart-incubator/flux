package com.flipkart.flux.exception;

/***
 *   <code> ReplayableRetryExhaustException</code> indicates that the retry count for a replayable state have been exhausted.
 *
 * @author vartika.bhatia
 */
public class ReplayableRetryExhaustException extends RuntimeException {

    public ReplayableRetryExhaustException(String message) {
        super(message);
    }
}

