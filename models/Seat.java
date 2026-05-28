package com.airline.skybooker.models;


import com.airline.skybooker.enums.SeatStatus;

public class Seat {
    private int seatId;
    private Flight flight;
    private String seatNumber;
    private String seatClass; // e.g., "ECONOMY", "BUSINESS"
    private int row;
    private String column;
    private boolean isWindow;
    private boolean isAisle;
    private boolean hasExtraLegroom;
    private SeatStatus status;
    private double priceMultiplier;

    // Comprehensive Constructor
    public Seat(int seatId, Flight flight, String seatNumber, String seatClass, int row, String column, boolean isWindow, boolean isAisle, boolean hasExtraLegroom) {
        this.seatId = seatId;
        this.flight = flight;
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.row = row;
        this.column = column;
        this.isWindow = isWindow;
        this.isAisle = isAisle;
        this.hasExtraLegroom = hasExtraLegroom;
        this.status = SeatStatus.AVAILABLE; // Default state
        this.priceMultiplier = 1.0;  // Default multiplier
    }

    // Getters
    public int getSeatId() { return seatId; }
    public Flight getFlight() { return flight; }
    public String getSeatNumber() { return seatNumber; }
    public String getSeatClass() { return seatClass; }
    public int getRow() { return row; }
    public String getColumn() { return column; }
    public boolean isWindow() { return isWindow; }
    public boolean isAisle() { return isAisle; }
    public boolean hasExtraLegroom() { return hasExtraLegroom; }
    public SeatStatus getStatus() { return status; }
    public double getPriceMultiplier() { return priceMultiplier; }

    // Setters
    public void setStatus(SeatStatus status) { this.status = status; }
    public void setPriceMultiplier(double priceMultiplier) { this.priceMultiplier = priceMultiplier; }

    // Overriding equals and hashCode for Collections operations later
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Seat seat = (Seat) obj;
        return seatId == seat.seatId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(seatId);
    }
}