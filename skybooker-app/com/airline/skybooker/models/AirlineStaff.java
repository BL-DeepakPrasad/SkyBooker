package com.airline.skybooker.models;

import com.airline.skybooker.enums.Role;

/**
 * Authorized personnel operating on behalf of a specific airline.
 * Provides access to operational controls like flight management and manifests.
 */
public class AirlineStaff extends User {

    /**
     * Initializes a new airline staff member with basic credentials.
     * Assigns the AIRLINE_STAFF role for operational access.
     *
     * @param userId       unique identifier for the staff member
     * @param fullName     complete legal name
     * @param email        primary contact email address
     * @param passwordHash secured hash of the login password
     * @param phone        primary contact phone number
     */
    public AirlineStaff(int userId, String fullName, String email, String passwordHash, String phone) {
        super(userId, fullName, email, passwordHash, phone, Role.AIRLINE_STAFF);
    }

    /**
     * Renders the airline staff dashboard interface to the console.
     * Exposes operational tools for managing flights, manifests, and schedules.
     */
    @Override
    public void displayDashboard() {
        System.out.println("\n=== AIRLINE STAFF DASHBOARD ===");
        System.out.println("Welcome, Staff Member " + getFullName() + "!");
        System.out.println("1. Manage Flights");
        System.out.println("2. View Passenger Manifests");
        System.out.println("3. Manage Crew Schedules");
    }
}
