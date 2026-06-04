package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Represents a booking waiting for the bank or credit card to approve the charge.
 * It exists to put the booking on hold so the seats aren't given away while the user is paying.
 */
public class PaymentPendingState implements BookingState {

    @Override
    public void nextState(Booking booking) {
        System.out.println("Payment successful! Transitioning to CONFIRMED...");
        booking.setState(new ConfirmedState());
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Payment failed or cancelled. Releasing seat...");
        booking.setState(new CancelledState());
    }

    @Override
    public String getStatusName() {
        return "PAYMENT_PENDING";
    }
}
