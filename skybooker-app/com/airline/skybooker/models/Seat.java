package com.airline.skybooker.models;

import com.airline.skybooker.enums.SeatType;

/**
 * Individual physical seat on a plane.
 * We need this class so passengers can pick specific spots (like '12A'), and so the system can lock a seat while someone is typing in their credit card to prevent double-booking.
 */
public class Seat {
    private String seatNumber;
    private boolean isBooked;
    private boolean isLocked;
    private SeatType type;

    /**
     * Creates a new seat when the system is building a plane's layout.
     * New seats are empty (unbooked) and available (unlocked) by default.
     *
     * @param seatNumber the label on the physical chair (e.g., "12A")
     * @param type       the class of the seat (e.g., Economy, Business) which changes the price
     */
    public Seat(String seatNumber, SeatType type) {
        this.seatNumber = seatNumber;
        this.type = type;
        this.isBooked = false;
        this.isLocked = false;
    }

    public String getSeatNumber() { return seatNumber; }
    public boolean isLocked() { return isLocked; }
    public boolean isBooked() { return isBooked; }
    public SeatType getType() { return type; }

    public void setLocked(boolean locked) { isLocked = locked; }
    public void setBooked(boolean booked) { isBooked = booked; }
}
