package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Defines the rules for a booking's lifecycle (like moving from 'Pending' to 'Confirmed').
 * It exists to make sure a booking can only move to valid states and can be cancelled properly.
 */
public interface BookingState {
    
    /**
     * Moves the booking forward to the next step (e.g., from 'Seat Selected' to 'Payment').
     * 
     * @param booking The booking that is moving forward
     */
    void nextState(Booking booking);

    /**
     * Cancels the current booking and handles any necessary clean-up, like releasing seats or issuing refunds.
     * 
     * @param booking The booking being cancelled
     */
    void cancel(Booking booking);

    /**
     * Returns the name of the current state (like "CONFIRMED" or "CANCELLED") so we can show it on the UI.
     * 
     * @return The simple text name of the state
     */
    String getStatusName();
}
