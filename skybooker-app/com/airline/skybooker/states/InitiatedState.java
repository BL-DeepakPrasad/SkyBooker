package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Represents a brand new booking that was just started.
 * It exists to hold the booking while the user is typing in their passenger details.
 */
public class InitiatedState implements BookingState {

    @Override
    public void nextState(Booking booking) {
        System.out.println("Transitioning from INITIATED -> PASSENGER_DETAILS...");
        booking.setState(new PassengerDetailsState());
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Booking cancelled during initialization.");
        booking.setState(new CancelledState());
    }

    @Override
    public String getStatusName() {
        return "INITIATED";
    }
}
