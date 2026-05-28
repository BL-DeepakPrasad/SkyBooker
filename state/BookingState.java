package com.airline.skybooker.state;
import com.airline.skybooker.models.Booking;

public interface BookingState {
    void next(Booking booking);
    void cancel(Booking booking);
    String getStatusString();
}
