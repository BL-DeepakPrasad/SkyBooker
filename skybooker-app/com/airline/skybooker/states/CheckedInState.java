package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * State representing a passenger who has successfully checked in online.
 */
public class CheckedInState implements BookingState {

    @Override
    public void nextState(Booking booking) {
        System.out.println("Booking is already CHECKED_IN. No further transitions available before boarding.");
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Initiating refund process and cancelling checked-in booking...");
        // In real life, checking in might incur higher cancellation fees, but we will reuse CancelledState.
        booking.setState(new CancelledState());
    }

    @Override
    public String getStatusName() {
        return "CHECKED_IN";
    }
}
