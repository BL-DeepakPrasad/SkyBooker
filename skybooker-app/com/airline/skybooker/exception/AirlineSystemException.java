package com.airline.skybooker.exception;

/**
 * Core foundational exception handling unexpected environmental or business rule violations.
 * Establishes a unified catch boundary for all internally derived application faults.
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
