package com.airline.skybooker.states;

import com.airline.skybooker.models.Booking;

/**
 * Represents the data collection phase where demographic and personal traveler information is actively captured.
 * Allows transition into inventory selection once mandatory identity fields are populated.
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
