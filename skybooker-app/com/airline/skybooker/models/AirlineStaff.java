package com.airline.skybooker.models;

import com.airline.skybooker.enums.Role;

/**
 * Airline Staff role extending User.
 */
public class AirlineStaff extends User {

    public AirlineStaff(int userId, String fullName, String email, String passwordHash, String phone) {
        super(userId, fullName, email, passwordHash, phone, Role.AIRLINE_STAFF);
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n=== AIRLINE STAFF DASHBOARD ===");
        System.out.println("Welcome, Staff Member " + getFullName() + "!");
        System.out.println("1. Manage Flights");
        System.out.println("2. View Passenger Manifests");
        System.out.println("3. Manage Crew Schedules");
    }
}
