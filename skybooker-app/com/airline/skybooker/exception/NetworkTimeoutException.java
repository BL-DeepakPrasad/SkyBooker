package com.airline.skybooker.exception;

/**
 * Thrown when a remote service (like a payment gateway) takes too long to respond.
 */
public class NetworkTimeoutException extends AirlineSystemException {
    /**
     * Instantiates a timeout exception for an interrupted upstream operation.
     * 
     * @param message Diagnostic details of the expired network request
     */
    public NetworkTimeoutException(String message) {
        super(message);
    }
}
