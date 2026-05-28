package com.airline.skybooker.models;

/**
 * Represents a specific physical seat on an aircraft.
 * This class is designed to easily map to a JPA Entity in Spring Boot.
 */
public class Seat {
    private String seatNumber;
    private boolean isWindow;
    private boolean isAisle;
    private boolean isLocked;
    private boolean isBooked;

    public Seat(String seatNumber, boolean isWindow, boolean isAisle) {
        this.seatNumber = seatNumber;
        this.isWindow = isWindow;
        this.isAisle = isAisle;
        this.isLocked = false;
        this.isBooked = false;
    }

    public String getSeatNumber() { return seatNumber; }
    public boolean isWindow() { return isWindow; }
    public boolean isAisle() { return isAisle; }
    public boolean isLocked() { return isLocked; }
    public boolean isBooked() { return isBooked; }

    public void setLocked(boolean locked) { isLocked = locked; }
    public void setBooked(boolean booked) { isBooked = booked; }
}
