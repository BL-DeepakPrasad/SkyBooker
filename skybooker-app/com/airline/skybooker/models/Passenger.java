package com.airline.skybooker.models;

import com.airline.skybooker.enums.Role;

/**
 * Normal user who logs in to book flights and manage their trips.
 * We need this class to store extra travel information, like passport numbers, that a standard 'User' doesn't have.
 */
public class Passenger extends User {
    private String passportNumber;
    private String nationality;
    private boolean whatsappOptIn;

    /**
     * Creates a new passenger account.
     * We automatically set their role to 'PASSENGER' so they only see customer menus, not admin menus.
     *
     * @param userId         unique database ID for the customer
     * @param fullName       their real name, matching their passport
     * @param email          email address for logging in and getting tickets
     * @param passwordHash   encrypted version of their password
     * @param phone          contact phone number
     * @param passportNumber travel document ID required for international flights
     * @param nationality    country they are a citizen of
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
     * Shows the main menu screen specifically designed for customers.
     * It overrides the basic User dashboard to display options like viewing their own bookings instead of system tools.
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
