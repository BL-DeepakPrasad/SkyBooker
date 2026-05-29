package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * The initial state when a user selects a flight to book.
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
