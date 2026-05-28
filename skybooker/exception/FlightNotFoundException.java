package com.airline.skybooker.exception;

public class FlightNotFoundException extends AirlineSystemException {
    public FlightNotFoundException(String origin, String destination) {
        super("No available flights found from " + origin + " to " + destination + ".");
    }
}
