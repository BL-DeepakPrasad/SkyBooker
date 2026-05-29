package com.airline.skybooker.models;

/**
 * Airline Staff role extending User.
 */
public class AirlineStaff extends User {

    public AirlineStaff(int userId, String fullName, String email, String passwordHash, String phone) {
        super(userId, fullName, email, passwordHash, phone, "AIRLINE_STAFF");
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n=== AIRLINE STAFF DASHBOARD ===");
        System.out.println("Welcome, Staff Member " + getFullName() + "!");
        System.out.println("1. Manage Flights");
        System.out.println("2. Update Flight Status");
        System.out.println("3. View Passenger Manifest");
    }
}
