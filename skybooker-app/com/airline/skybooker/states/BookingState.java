package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Defines the rules for a booking's lifecycle (like moving from 'Pending' to 'Confirmed').
 * It exists to make sure a booking can only move to valid states and can be cancelled properly.
 */
public interface BookingState {
    
    /**
     * Advances the reservation context to the subsequent logical phase in the checkout or fulfillment pipeline.
     * 
     * @param booking The stateful context object undergoing mutation
     */
    void nextState(Booking booking);

    /**
     * Terminates the active reservation flow, potentially triggering refunds or inventory releases depending on the current phase.
     * 
     * @param booking The stateful context object undergoing termination
     */
    void cancel(Booking booking);

    /**
     * Retrieves the formalized string representation of the current operational phase.
     * 
     * @return The standard string identifier for the state
     */
    String getStatusName();
}
