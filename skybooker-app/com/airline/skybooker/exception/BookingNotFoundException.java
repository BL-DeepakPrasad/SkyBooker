package com.airline.skybooker.exception;

/**
 * Signals that a requested reservation entity could not be located within the active persistence store.
 * Triggered during PNR lookup or post-booking retrieval attempts.
 */
public class BookingNotFoundException extends AirlineSystemException {
    /**
     * Constructs a domain exception denoting a missing booking record.
     * 
     * @param message The diagnostic reason detailing the missing reservation identifier
     */
    public BookingNotFoundException(String message) {
        super(message);
    }
}
