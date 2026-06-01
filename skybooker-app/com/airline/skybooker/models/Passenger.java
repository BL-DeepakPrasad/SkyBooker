package com.airline.skybooker.models;

import com.airline.skybooker.enums.Role;

/**
 * Passenger role extending User.
 * Demonstrates OOAD Inheritance.
 */
public class Passenger extends User {
    private String passportNumber;
    private String nationality;
    private boolean whatsappOptIn;

    public Passenger(int userId, String fullName, String email, String passwordHash, String phone, String passportNumber, String nationality) {
        super(userId, fullName, email, passwordHash, phone, Role.PASSENGER);
        this.passportNumber = passportNumber;
        this.nationality = nationality;
        this.whatsappOptIn = false;
    }

    public String getPassportNumber() { return passportNumber; }
    public String getNationality() { return nationality; }
    public boolean isWhatsappOptIn() { return whatsappOptIn; }

    public void setPassportNumber(String passport) { this.passportNumber = passport; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public void setWhatsappOptIn(boolean whatsappOptIn) { this.whatsappOptIn = whatsappOptIn; }

    @Override
    public void displayDashboard() {
        System.out.println("\n=== PASSENGER DASHBOARD ===");
        System.out.println("Welcome, " + getFullName() + "!");
        System.out.println("Passport: " + passportNumber);
        System.out.println("Nationality: " + nationality);
        System.out.println("WhatsApp Alerts: " + (whatsappOptIn ? "ON" : "OFF"));
        System.out.println("1. View Full Profile");
        System.out.println("2. View / Cancel Bookings");
        System.out.println("3. Update Profile");
    }
}
