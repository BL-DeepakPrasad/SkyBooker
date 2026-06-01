package com.airline.skybooker.exception;

/**
 * Indicates a critical failure in establishing or maintaining a session with the persistent data store.
 * Generally raised when connection pools are exhausted or the database server is unreachable.
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
