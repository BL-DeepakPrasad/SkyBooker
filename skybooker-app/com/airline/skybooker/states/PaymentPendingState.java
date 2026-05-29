package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

public class PaymentPendingState implements BookingState {

    @Override
    public void nextState(Booking booking) {
        System.out.println("Payment successful! Booking is now CONFIRMED.");
        booking.setPnrCode("PNR" + (int)(Math.random() * 10000));
        // We will transition to ConfirmedState in a future UC, but for now we'll just leave it here or make a ConfirmedState.
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
