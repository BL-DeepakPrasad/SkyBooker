package com.airline.skybooker.filters;

import com.airline.skybooker.models.Flight;
import java.util.function.Predicate;

/**
 * Criteria that filters flights to strictly match a specific airline ID.
 */
public class AirlineCriteria implements FlightCriteria {
    private final Integer airlineId;

    public AirlineCriteria(Integer airlineId) {
        this.airlineId = airlineId;
    }

    @Override
    public Predicate<Flight> meetCriteria() {
        return flight -> airlineId == null || flight.getAirline().getAirlineId() == airlineId;
    }
}
