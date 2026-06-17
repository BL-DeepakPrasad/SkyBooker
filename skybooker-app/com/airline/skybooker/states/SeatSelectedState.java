package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Represents a booking where the user has picked their seats.
 * It exists to lock those seats temporarily so nobody else can take them before payment is complete.
 */
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
