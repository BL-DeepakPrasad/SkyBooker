package com.airline.skybooker.models;

/**
 * Represents a passenger line item within a Booking.
 */
public class BookingPassenger {
    private String fullName;
    private String passportNumber;
    private String ageCategory; // Adult, Child, Infant
    private String seatNumber;
    private double baggageWeight; // in kg
    private boolean mealUpgrade;
    private boolean isCancelled;
    private double farePaid;

    public BookingPassenger(String fullName, String passportNumber, String ageCategory) {
        this.fullName = fullName;
        this.passportNumber = passportNumber;
        this.ageCategory = ageCategory;
        this.baggageWeight = 15.0; // Default standard baggage
        this.mealUpgrade = false;
        this.isCancelled = false;
        this.farePaid = 0.0;
    }

    public String getFullName() { return fullName; }
    public String getPassportNumber() { return passportNumber; }
    public String getAgeCategory() { return ageCategory; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public double getBaggageWeight() { return baggageWeight; }
    public void setBaggageWeight(double baggageWeight) { this.baggageWeight = baggageWeight; }
    public boolean hasMealUpgrade() { return mealUpgrade; }
    public void setMealUpgrade(boolean mealUpgrade) { this.mealUpgrade = mealUpgrade; }
    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean cancelled) { this.isCancelled = cancelled; }
    public double getFarePaid() { return farePaid; }
    public void setFarePaid(double farePaid) { this.farePaid = farePaid; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }
}
