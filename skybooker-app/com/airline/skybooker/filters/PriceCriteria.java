package com.airline.skybooker.filters;

import com.airline.skybooker.models.Flight;
import java.util.function.Predicate;

/**
 * Criteria that filters flights based on a maximum acceptable base price.
 */
public class PriceCriteria implements FlightCriteria {
    private final Double maxPrice;

    public PriceCriteria(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    @Override
    public Predicate<Flight> meetCriteria() {
        return flight -> maxPrice == null || flight.getBasePrice() <= maxPrice;
    }
}
