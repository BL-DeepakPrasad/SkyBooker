package com.airline.skybooker.interfaces;

import com.airline.skybooker.models.Flight;
import java.util.List;

/**
 * Defines the contract for services that support searching for flight data.
 * Implementations of this interface should provide mechanisms to retrieve
 * flights based on various filtering parameters such as route or airline.
 */
public interface Searchable {

    /**
     * Searches for available flights operating between the specified origin and destination.
     *
     * @param origin      the IATA code of the departure airport
     * @param destination the IATA code of the arrival airport
     * @return a list of {@link Flight} objects matching the criteria
     * @throws com.airline.skybooker.exception.FlightNotFoundException if no active flights are found
     */
    List<Flight> searchFlights(String origin, String destination);

    /**
     * Retrieves all scheduled flights operated by a specific airline.
     *
     * @param airlineId the unique identifier of the airline
     * @return a list of {@link Flight} objects operated by the airline, or an empty list if none are found
     */
    List<Flight> getFlightsByAirline(int airlineId);
}
