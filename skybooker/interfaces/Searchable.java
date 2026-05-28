package com.airline.skybooker.interfaces;

import com.airline.skybooker.models.Flight;
import java.util.List;

public interface Searchable {
    List<Flight> searchFlights(String origin, String destination);
    List<Flight> getFlightsByAirline(int airlineId);
}
