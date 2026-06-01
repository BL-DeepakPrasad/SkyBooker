package com.airline.skybooker.enums;

/**
 * Categorizes reservation precedence to dictate processing order within concurrent queues.
 * Determines queue prioritization strategies for backend confirmation workflows.
 */
public enum BookingPriority {
    EXPRESS, // Processed First
    REGULAR  // Processed Second
}
