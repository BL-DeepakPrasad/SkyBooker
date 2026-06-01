package com.airline.skybooker.models;

import com.airline.skybooker.enums.Role;

/**
 * Standard customer entity utilizing the platform to search and book flights.
 * Maintains travel-specific profile data such as passport details and preferences.
 */
public class Passenger extends User {
    private String passportNumber;
    private String nationality;
    private boolean whatsappOptIn;

    /**
     * Registers a new passenger with comprehensive personal and travel details.
     * Inherits base authentication properties and automatically assigns the PASSENGER role.
     *
     * @param userId         unique identifier for the customer
     * @param fullName       complete legal name
     * @param email          primary contact email address
     * @param passwordHash   secured hash of the login password
     * @param phone          primary contact phone number
     * @param passportNumber official travel document identifier
     * @param nationality    country of citizenship
     */
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

    /**
     * Renders the personalized passenger portal to the console.
     * Displays profile summary and interactive menu options for booking management.
     */
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
