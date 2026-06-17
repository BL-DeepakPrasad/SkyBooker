package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Represents a booking that has been paid for and finalized.
 * It exists to show the user they have a guaranteed seat and can now check in.
 */
public class ConfirmedState implements BookingState {

    @Override
    public void nextState(Booking booking) {
        System.out.println("Transitioning booking from CONFIRMED to CHECKED_IN...");
        booking.setState(new CheckedInState());
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Initiating refund process and cancelling confirmed booking...");
        booking.setState(new CancelledState());
    }

    @Override
    public String getStatusName() {
        return "CONFIRMED";
    }
}
