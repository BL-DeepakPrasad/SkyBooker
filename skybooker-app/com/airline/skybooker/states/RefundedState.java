package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Represents a cancelled booking where the user has gotten their money back.
 * It exists to show that the financial part of the cancellation is completely finished.
 */
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
