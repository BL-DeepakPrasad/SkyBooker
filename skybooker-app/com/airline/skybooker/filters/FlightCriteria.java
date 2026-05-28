package com.airline.skybooker.filters;

import com.airline.skybooker.models.Flight;
import java.util.function.Predicate;

/**
 * The FlightCriteria interface defines the contract for all flight filters.
 * It utilizes Java's Predicate functional interface to seamlessly integrate
 * with the Streams API.
 */
public interface FlightCriteria {
    
    /**
     * Provides the filtering logic.
     *
     * @return a Predicate evaluating whether a Flight meets the criteria
     */
    Predicate<Flight> meetCriteria();
}
