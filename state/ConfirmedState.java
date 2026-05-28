package com.airline.skybooker.state;
import com.airline.skybooker.models.Booking;

public class ConfirmedState implements BookingState {
    @Override
    public void next(Booking booking) {
        System.out.println("Booking is already confirmed.");
    }
    @Override
    public void cancel(Booking booking) {
        booking.setState(new CancelledState());
    }
    @Override
    public String getStatusString() { return "CONFIRMED"; }
}
