package com.airline.skybooker.exception;

/**
 * Thrown to indicate that no flights were found for a specific search criteria.
 * This usually occurs when searching for routes that have no scheduled flights or
 * when all scheduled flights are fully booked.
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
