package com.airline.skybooker.state;
import com.airline.skybooker.models.Booking;

public class PaymentPendingState implements BookingState {
    @Override
    public void next(Booking booking) {
        booking.setState(new ConfirmedState());
    }
    @Override
    public void cancel(Booking booking) {
        booking.setState(new CancelledState());
    }
    @Override
    public String getStatusString() { return "PAYMENT_PENDING"; }
}
