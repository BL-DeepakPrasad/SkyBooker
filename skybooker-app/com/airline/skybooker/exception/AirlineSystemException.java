package com.airline.skybooker.exception;

/**
 * Represents a generic system exception within the reservation platform.
 * This serves as the base class for all custom domain exceptions.
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
