package com.airline.skybooker.models;

import com.airline.skybooker.states.BookingState;
import com.airline.skybooker.states.InitiatedState;
import com.airline.skybooker.enums.BookingPriority;
import com.airline.skybooker.payments.PaymentStrategy;
import java.time.LocalDateTime;

public class Booking implements Comparable<Booking> {
    private String bookingId;
    private int userId;
    private int flightId;
    private String pnrCode;
    private double totalFare;
    private LocalDateTime bookedAt;
    private java.util.List<BookingPassenger> passengers;
    
    // State Pattern Context Variable
    private BookingState currentState;
    private BookingPriority priority;
    private long timestamp;
    private PaymentStrategy paymentStrategy;

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
    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setPriority(BookingPriority priority) { this.priority = priority; }
    public BookingPriority getPriority() { return priority; }
    public long getTimestamp() { return timestamp; }
    public void setPaymentStrategy(PaymentStrategy strategy) { this.paymentStrategy = strategy; }
    public PaymentStrategy getPaymentStrategy() { return paymentStrategy; }
    public java.util.List<BookingPassenger> getPassengers() { return passengers; }
}
