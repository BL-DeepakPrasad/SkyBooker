package com.airline.skybooker.exception;

/**
 * A general error for the SkyBooker application.
 * All other custom errors in this application are based on this one.
 */
public class AirlineSystemException extends RuntimeException {

    /**
     * Constructs a new AirlineSystemException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public AirlineSystemException(String message) {
        super(message);
    }
}
