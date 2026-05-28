package com.airline.skybooker.models;

import com.airline.skybooker.enums.TripType;
import com.airline.skybooker.state.BookingState;
import com.airline.skybooker.state.InitiatedState;

import java.time.LocalDateTime;
import java.util.List;


public class Booking {
    private String bookingId;
    private int userId;
    private Flight flight;
    private Flight returnFlight;
    private List<Passenger> passengers;
    private TripType tripType;
    private BookingState state;
    private double totalFare;
    private LocalDateTime bookedAt;

    public Booking(String bookingId, int userId, Flight flight, Flight returnFlight, List<Passenger> passengers, TripType tripType, double totalFare) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.flight = flight;
        this.returnFlight = returnFlight;
        this.passengers = passengers;
        this.tripType = tripType;
        this.totalFare = totalFare;
        this.state = new InitiatedState();
        this.bookedAt = LocalDateTime.now();
    }

    public String getBookingId() { return bookingId; }
    public int getUserId() { return userId; }
    public TripType getTripType() { return tripType; }
    public double getTotalFare() { return totalFare; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    
    public void setState(BookingState state) { this.state = state; }
    public BookingState getState() { return state; }
    public void proceedToNextState() { state.next(this); }
    public void cancelBooking() { state.cancel(this); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Booking ID: ").append(bookingId)
          .append(" | Outbound Flight: ").append(flight.getFlightNumber());
        if (returnFlight != null) {
            sb.append(" | Return Flight: ").append(returnFlight.getFlightNumber());
        }
        sb.append(" | Type: ").append(tripType)
          .append(" | Total Cost: $").append(totalFare)
          .append(" | Status: ").append(state.getStatusString());
        
        if (passengers != null && !passengers.isEmpty()) {
            for (Passenger p : passengers) {
                sb.append("\n  -> ").append(p.toString());
            }
        }
        return sb.toString();
    }
}