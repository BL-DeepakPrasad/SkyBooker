package com.airline.skybooker.exception;

/**
 * Thrown when no flights are available for the cities or dates the user searched for.
 */
public class FlightNotFoundException extends AirlineSystemException {

    /**
     * Constructs a new FlightNotFoundException with a descriptive message
     * specifying the origin and destination that failed to yield results.
     *
     * @param origin      the IATA code of the origin airport
     * @param destination the IATA code of the destination airport
     */
    public FlightNotFoundException(String origin, String destination) {
        super("No available flights found from " + origin + " to " + destination + ".");
    }
}
