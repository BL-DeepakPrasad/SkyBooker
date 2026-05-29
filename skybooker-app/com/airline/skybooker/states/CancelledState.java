package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

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
