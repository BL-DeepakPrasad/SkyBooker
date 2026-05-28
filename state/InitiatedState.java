package com.airline.skybooker.state;
import com.airline.skybooker.models.Booking;

public class InitiatedState implements BookingState {
    @Override
    public void next(Booking booking) {
        booking.setState(new PaymentPendingState());
    }
    @Override
    public void cancel(Booking booking) {
        booking.setState(new CancelledState());
    }
    @Override
    public String getStatusString() { return "INITIATED"; }
}
