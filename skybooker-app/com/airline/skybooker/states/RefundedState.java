package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

public class RefundedState implements BookingState {

    @Override
    public void nextState(Booking booking) {
        System.out.println("Booking is fully refunded and closed. No further transitions.");
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Booking is already refunded and cancelled.");
    }

    @Override
    public String getStatusName() {
        return "REFUNDED";
    }
}
