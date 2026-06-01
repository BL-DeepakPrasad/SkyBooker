package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Denotes a fully settled reservation encompassing successful payment and locked inventory.
 * Exposes operations for progressing to airport check-in or orchestrating post-payment refunds.
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
