package com.airline.skybooker.exception;

/**
 * Signals a concurrency collision or availability fault during inventory allocation.
 * Triggered when multiple threads attempt to mutate identical seating resources simultaneously.
 */
public class SeatLockException extends AirlineSystemException {
    public SeatLockException(String message) {
        super(message);
    }
}
