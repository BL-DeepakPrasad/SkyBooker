package com.airline.skybooker.models;

import com.airline.skybooker.enums.SeatType;

/**
 * Represents a specific physical seat on an aircraft.
 * This class is designed to easily map to a JPA Entity in Spring Boot.
 */
public class Seat {
    private String seatNumber;
    private boolean isBooked;
    private boolean isLocked;
    private SeatType type;

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
