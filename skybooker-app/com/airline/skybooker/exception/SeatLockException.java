package com.airline.skybooker.exception;

/**
 * Thrown when a seat cannot be reserved.
 * This usually happens if someone else just booked the same seat a moment ago.
 */
public class SeatLockException extends AirlineSystemException {
    public SeatLockException(String message) {
        super(message);
    }
}
