package com.airline.skybooker.exception;

/**
 * Thrown when the application cannot talk to the database.
 * This might happen if the internet is down or the database server is turned off.
 */
public class DatabaseConnectionException extends AirlineSystemException {
    /**
     * Initializes the connection failure exception with a specific environmental cause.
     * 
     * @param message Contextual details surrounding the data source failure
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }
}
