package com.airline.skybooker.exception;

public class BookingNotFoundException extends AirlineSystemException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
