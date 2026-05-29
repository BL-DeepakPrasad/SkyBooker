package com.airline.skybooker.enums;

/**
 * Defines priority levels for processing bookings.
 * Used by the PriorityBlockingQueue.
 */
public enum BookingPriority {
    EXPRESS, // Processed First
    REGULAR  // Processed Second
}
