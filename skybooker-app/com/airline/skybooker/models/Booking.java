package com.airline.skybooker.models;

import com.airline.skybooker.states.BookingState;
import com.airline.skybooker.states.InitiatedState;
import com.airline.skybooker.enums.BookingPriority;
import com.airline.skybooker.interfaces.Payable;
import com.airline.skybooker.interfaces.Bookable;
import com.airline.skybooker.interfaces.Cancellable;
import java.time.LocalDateTime;

/**
 * Flight reservation for one or more passengers.
 * Orchestrates the booking lifecycle using the State Pattern, managing transitions 
 * from initialization through payment and final confirmation or cancellation.
 */
public class Booking implements Comparable<Booking>, Bookable, Cancellable {
    private String bookingId;
    private int userId;
    private int flightId;
    private String pnrCode;
    private double totalFare;
    private String fareBreakdown;
    private LocalDateTime bookedAt;
    private java.util.List<BookingPassenger> passengers;
    
    // State Pattern Context Variable
    private BookingState currentState;
    private BookingPriority priority;
    private long timestamp;
    private Payable payable;

    /**
     * Initializes a new booking session for a user on a specific flight.
     * Generates a unique PNR and sets the initial state to Initiated.
     *
     * @param bookingId unique identifier for the booking system
     * @param userId    ID of the user making the reservation
     * @param flightId  ID of the selected flight
     */
    public Booking(String bookingId, int userId, int flightId) {
        this.bookingId = bookingId;
        this.pnrCode = "PNR" + (int)(Math.random() * 10000);
        this.userId = userId;
        this.flightId = flightId;
        this.bookedAt = LocalDateTime.now();
        this.timestamp = System.currentTimeMillis();
        this.priority = BookingPriority.REGULAR;
        this.passengers = new java.util.ArrayList<>();
        
        // Initialize state
        this.currentState = new InitiatedState();
    }

    // State Pattern Delegation Methods
    /**
     * Triggers the transition to the next logical state in the booking lifecycle.
     * Delegation is handled by the current BookingState implementation.
     */
    public void nextState() {
        currentState.nextState(this);
    }

    @Override
    public boolean cancel() {
        currentState.cancel(this);
        return true;
    }

    @Override
    public void confirm() {
        // Confirmation logic is typically handled by PaymentPendingState -> ConfirmedState
        // This is added to fulfill the Bookable interface requirement.
    }

    public String getStatus() {
        return currentState.getStatusName();
    }

    public void setState(BookingState state) {
        this.currentState = state;
    }

    /**
     * Compares this booking with another to establish processing priority.
     * Priority bookings (e.g., EXPRESS) are processed first, followed by timestamp ordering.
     *
     * @param other the booking to compare against
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
     */
    @Override
    public int compareTo(Booking other) {
        // 1. Sort by Priority (EXPRESS comes before REGULAR)
        if (this.priority != other.priority) {
            // EXPRESS is defined first in Enum, so its ordinal is 0. 
            // We want lower ordinal to come first.
            return Integer.compare(this.priority.ordinal(), other.priority.ordinal());
        }
        // 2. If priorities are equal, sort by Timestamp (older requests processed first)
        return Long.compare(this.timestamp, other.timestamp);
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public int getUserId() { return userId; }
    public int getFlightId() { return flightId; }
    public String getPnrCode() { return pnrCode; }
    public void setPnrCode(String pnrCode) { this.pnrCode = pnrCode; }
    public double getTotalFare() { return totalFare; }
    public void setTotalFare(double totalFare) { this.totalFare = totalFare; }
    public String getFareBreakdown() { return fareBreakdown; }
    public void setFareBreakdown(String fareBreakdown) { this.fareBreakdown = fareBreakdown; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setPriority(BookingPriority priority) { this.priority = priority; }
    public BookingPriority getPriority() { return priority; }
    public long getTimestamp() { return timestamp; }
    public void setPayable(Payable payable) { this.payable = payable; }
    public Payable getPayable() { return payable; }
    public java.util.List<BookingPassenger> getPassengers() { return passengers; }
}
