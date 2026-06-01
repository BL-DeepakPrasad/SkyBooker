package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Contract for the State Design Pattern orchestrating the reservation lifecycle.
 * Dictates allowable transitions and cancellation behaviors based on the current context phase.
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
