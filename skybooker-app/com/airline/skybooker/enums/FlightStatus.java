package com.airline.skybooker.enums;

/**
 * Represents the current state of a flight.
 * It exists so users and staff know if a flight is on time, delayed, or cancelled.
 */
public enum FlightStatus {
    SCHEDULED,
    DELAYED,
    CANCELLED
}
