package com.airline.skybooker.exception;

/**
 * Thrown when the system cannot find a specific booking.
 * Usually happens if the user enters a wrong booking ID or PNR code.
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
