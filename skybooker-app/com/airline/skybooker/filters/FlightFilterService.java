package com.airline.skybooker.filters;

import com.airline.skybooker.models.Flight;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A dedicated service module for processing flight filters.
 * It combines multiple FlightCriteria using the Java Streams API.
 */
public class FlightFilterService {

    /**
     * Applies an array of criteria to a list of flights.
     *
     * @param flights  the original list of flights
     * @param criteria an array of filters to apply (e.g., Price, Airline)
     * @return a new list containing only flights that pass ALL criteria
     */
    public List<Flight> filter(List<Flight> flights, FlightCriteria... criteria) {
        Stream<Flight> flightStream = flights.stream();

        // Chain the predicates dynamically
        for (FlightCriteria c : criteria) {
            flightStream = flightStream.filter(c.meetCriteria());
        }

        return flightStream.collect(Collectors.toList());
    }
}
