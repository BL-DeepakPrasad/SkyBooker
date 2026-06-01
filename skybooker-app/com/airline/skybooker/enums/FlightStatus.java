package com.airline.skybooker.enums;

/**
 * Represents the operational lifecycle phase of an individual scheduled flight.
 * Determines passenger eligibility for boarding, modifications, and compensation.
 */
public enum FlightStatus {
    SCHEDULED,
    DELAYED,
    CANCELLED
}
