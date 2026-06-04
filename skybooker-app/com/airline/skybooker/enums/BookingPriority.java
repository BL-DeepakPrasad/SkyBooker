package com.airline.skybooker.enums;

/**
 * Represents how fast a booking should be processed.
 * It exists to let some users pay extra to jump ahead in the booking queue.
 */
public enum BookingPriority {
    EXPRESS, // Processed First
    REGULAR  // Processed Second
}
