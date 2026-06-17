package com.airline.skybooker.models;

/**
 * Details of a specific person traveling on a ticket.
 * We need this class because one booking can have multiple people (e.g., a family of four), and each person has their own specific seat, passport, and baggage.
 */
public class BookingPassenger {
    private String fullName;
    private String passportNumber;
    private final String ageCategory; // Adult, Child, Infant
    private String seatNumber;
    private double baggageWeight; // in kg
    private boolean mealUpgrade;
    private boolean isCancelled;
    private double farePaid;

    /**
     * Creates a record for a person on the flight.
     * We give everyone a standard 15kg baggage allowance by default, which can be changed later if they buy extra bags.
     *
     * @param fullName       the passenger's full name, exactly as it appears on their ID
     * @param passportNumber their passport or ID number
     * @param ageCategory    whether they are an Adult, Child, or Infant (used to calculate ticket prices)
     */
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
