package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * The core State Interface for the State Design Pattern.
 * Defines the contract for all booking lifecycle states.
 */
public interface BookingState {
    
    /**
     * Transitions the booking to the next logical state.
     * @param booking the context
     */
    void nextState(Booking booking);

    /**
     * Cancels the booking from the current state.
     * @param booking the context
     */
    void cancel(Booking booking);

    /**
     * Gets the human-readable name of the current state.
     * @return state name
     */
    String getStatusName();
}
