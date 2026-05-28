package com.airline.skybooker.exception;

/**
 * Custom exception thrown when a seat cannot be locked due to 
 * concurrency issues, invalid seat numbers, or already being booked.
 */
public class SeatLockException extends AirlineSystemException {
    public SeatLockException(String message) {
        super(message);
    }
}
