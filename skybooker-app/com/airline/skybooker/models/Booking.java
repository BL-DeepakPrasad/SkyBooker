package com.airline.skybooker.models;

import com.airline.skybooker.states.BookingState;
import com.airline.skybooker.states.InitiatedState;
import com.airline.skybooker.enums.BookingPriority;
import com.airline.skybooker.interfaces.Payable;
import com.airline.skybooker.interfaces.Bookable;
import com.airline.skybooker.interfaces.Cancellable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record of a customer buying tickets for a specific flight.
 * We need this class to track the entire process of a purchase, from the moment they click "Book" to when they actually pay and get confirmed.
 * It uses the "State Pattern" to ensure a booking moves correctly step-by-step (e.g., you can't be 'Confirmed' before you are 'Paid').
 */
public class Booking implements Comparable<Booking>, Bookable, Cancellable {
    private String bookingId;
    private int userId;
    private int flightId;
    private String pnrCode;
    private double totalFare;
    private String fareBreakdown;
    private LocalDateTime bookedAt;
    private List<BookingPassenger> passengers;
    
    // State Pattern Context Variable
    private BookingState currentState;
    private BookingPriority priority;
    private long timestamp;
    private Payable payable;

    /**
     * Starts a brand new booking when a user decides to buy a ticket.
     * We give it a random PNR (booking reference) and set its initial state to "Initiated" so the system knows payment hasn't happened yet.
     *
     * @param bookingId unique ID for the booking database
     * @param userId    ID of the person buying the ticket
     * @param flightId  ID of the flight they want to get on
     */
    public Booking(String bookingId, int userId, int flightId) {
        this.bookingId = bookingId;
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
     * Moves the booking forward to the next logical step.
     * Instead of a messy block of if/else statements, the current state object itself decides what the "next" state should be.
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
     * Compares this booking against another one to decide which should be processed first.
     * We need this so that VIP/Express bookings skip the line, while normal bookings are handled in the order they were created.
     *
     * @param other the booking to compare against
     * @return a negative number if this booking goes first, positive if it goes after, or zero if they are tied
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
    public List<BookingPassenger> getPassengers() { return passengers; }
}
