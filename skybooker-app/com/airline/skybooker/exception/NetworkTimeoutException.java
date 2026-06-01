package com.airline.skybooker.exception;

/**
 * Denotes a latency breach when interfacing with external or distributed subsystems.
 * Utilized by components requiring synchronous responses within a predefined time-to-live threshold.
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
