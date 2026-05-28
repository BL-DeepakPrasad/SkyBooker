package com.airline.skybooker.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String id) { super("Booking ID " + id + " not found."); }
}
