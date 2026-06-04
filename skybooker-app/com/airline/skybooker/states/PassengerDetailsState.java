package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Represents a booking where the user has entered passenger names but hasn't picked seats yet.
 * It exists as a stepping stone before seat selection and payment.
 */
public class PassengerDetailsState implements BookingState {

    @Override
    public void nextState(Booking booking) {
        System.out.println("Passenger details captured. Transitioning to SEAT_SELECTED...");
        booking.setState(new SeatSelectedState());
    }

    @Override
    public void cancel(Booking booking) {
        System.out.println("Booking cancelled during passenger detail entry.");
        booking.setState(new CancelledState());
    }

    @Override
    public String getStatusName() {
        return "PASSENGER_DETAILS";
    }
}
