package com.airline.skybooker.models;

import com.airline.skybooker.enums.SeatType;

/**
 * Physical seating assignment on an aircraft.
 * Tracks reservation status and pricing tier via the seat type.
 * Designed to easily map to a persistent JPA Entity in enterprise frameworks.
 */
public class Seat {
    private String seatNumber;
    private boolean isBooked;
    private boolean isLocked;
    private SeatType type;

    /**
     * Instantiates a new seat configuration for a flight layout.
     * Initializes the seat as unoccupied and unlocked.
     *
     * @param seatNumber alphanumeric identifier (e.g., "12A")
     * @param type       classification of the seat dictating cost and features
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
