package com.airline.skybooker.exception;

public class SeatUnavailableException extends AirlineSystemException {
    public SeatUnavailableException(String seatNumber, String flightNumber) {
        super("Seat " + seatNumber + " on flight " + flightNumber + " is currently locked or unavailable.");
    }
}