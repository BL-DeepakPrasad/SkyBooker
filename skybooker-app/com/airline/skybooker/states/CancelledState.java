package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Represents a booking that has been permanently cancelled.
 * It exists to stop any further changes to a booking once it is cancelled.
 */
public class CancelledState implements BookingState {

    @Override
    public void nextState(Booking booking) {
        System.out.println("Cannot transition. Booking is already CANCELLED.");
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Booking is already CANCELLED.");
    }

    @Override
    public String getStatusName() {
        return "CANCELLED";
    }
}
