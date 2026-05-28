package com.airline.skybooker.state;
import com.airline.skybooker.models.Booking;

public class CancelledState implements BookingState {
    @Override
    public void next(Booking booking) {
        System.out.println("Cannot proceed. Booking is cancelled.");
    }
    @Override
    public void cancel(Booking booking) {
        System.out.println("Booking is already cancelled.");
    }
    @Override
    public String getStatusString() { return "CANCELLED"; }
}
