package com.flipkart.flux.client.exception;
/*
* This exception will be thrown whenever a duplicate request comes, for eg a already triggered event comes again
* */
public class DuplicateRequestException extends RuntimeException {

    public DuplicateRequestException(String message){ super(message);}

}
