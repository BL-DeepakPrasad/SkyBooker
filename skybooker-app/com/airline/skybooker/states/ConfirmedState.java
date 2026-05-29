package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

public class ConfirmedState implements BookingState {

    @Override
    public void nextState(Booking booking) {
        System.out.println("Booking is already CONFIRMED. No further transitions available.");
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
