package com.airline.skybooker.interfaces;


import com.airline.skybooker.enums.TripType;
import com.airline.skybooker.exception.SeatUnavailableException;
import com.airline.skybooker.models.*;

import java.util.List;

public interface Bookable {
    Booking createBooking(User user, Flight flight, List<Passenger> passengers, TripType tripType);
    Seat allocateSeat(Flight flight, Passenger passenger, String preferredSeat) throws SeatUnavailableException;
    void confirmBooking(String bookingId);
}