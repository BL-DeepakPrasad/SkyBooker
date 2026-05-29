package com.airline.skybooker.models;

import com.airline.skybooker.states.BookingState;
import com.airline.skybooker.states.InitiatedState;

import java.time.LocalDateTime;

/**
 * Represents a flight booking.
 * This class acts as the Context in the State Design Pattern.
 */
public class Booking {
    private String bookingId;
    private int userId;
    private int flightId;
    private String pnrCode;
    private double totalFare;
    private LocalDateTime bookedAt;
    
    // State Pattern Context Variable
    private BookingState currentState;

    public Booking(String bookingId, int userId, int flightId) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.flightId = flightId;
        this.bookedAt = LocalDateTime.now();
        
        // Initial state is always INITIATED
        this.currentState = new InitiatedState();
    }

    // State Pattern Delegation Methods
    public void nextState() {
        currentState.nextState(this);
    }

    public void cancel() {
        currentState.cancel(this);
    }

    public String getStatus() {
        return currentState.getStatusName();
    }

    public void setState(BookingState state) {
        this.currentState = state;
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public int getUserId() { return userId; }
    public int getFlightId() { return flightId; }
    public String getPnrCode() { return pnrCode; }
    public void setPnrCode(String pnrCode) { this.pnrCode = pnrCode; }
    public double getTotalFare() { return totalFare; }
    public void setTotalFare(double totalFare) { this.totalFare = totalFare; }
    public LocalDateTime getBookedAt() { return bookedAt; }
}
