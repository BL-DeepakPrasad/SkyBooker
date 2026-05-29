package com.airline.skybooker.models;

/**
 * Passenger role extending User.
 * Demonstrates OOAD Inheritance.
 */
public class Passenger extends User {
    private String passportNumber;
    private String nationality;

    public Passenger(int userId, String fullName, String email, String passwordHash, String phone, String passportNumber, String nationality) {
        super(userId, fullName, email, passwordHash, phone, "PASSENGER");
        this.passportNumber = passportNumber;
        this.nationality = nationality;
    }

    public String getPassportNumber() { return passportNumber; }
    public String getNationality() { return nationality; }

    @Override
    public void displayDashboard() {
        System.out.println("\n=== PASSENGER DASHBOARD ===");
        System.out.println("Welcome, " + getFullName() + "!");
        System.out.println("1. Search Flights");
        System.out.println("2. View My Bookings");
        System.out.println("3. Update Profile");
    }
}
