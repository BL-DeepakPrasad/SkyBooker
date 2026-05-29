package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

public class SeatSelectedState implements BookingState {

    @Override
    public void nextState(Booking booking) {
        System.out.println("Seat locked. Transitioning to PAYMENT_PENDING...");
        booking.setState(new PaymentPendingState());
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Booking cancelled. Releasing locked seat...");
        booking.setState(new CancelledState());
    }

    @Override
    public String getStatusName() {
        return "SEAT_SELECTED";
    }
}
