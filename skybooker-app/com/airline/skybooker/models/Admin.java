package com.airline.skybooker.models;

import com.airline.skybooker.enums.Role;

/**
 * Administrator entity responsible for system-wide configuration and oversight.
 * Demonstrates OOAD Inheritance and Polymorphism.
 */
public class Admin extends User {

    /**
     * Initializes a new administrator with required credentials and contact details.
     * Assigns the highest authorization level (ADMIN) by default.
     *
     * @param userId       unique identifier for the administrator
     * @param fullName     complete legal name
     * @param email        primary contact email address
     * @param passwordHash secured hash of the login password
     * @param phone        primary contact phone number
     */
    public Admin(int userId, String fullName, String email, String passwordHash, String phone) {
        super(userId, fullName, email, passwordHash, phone, Role.ADMIN);
    }

    /**
     * Renders the administrative dashboard interface to the console.
     * Exposes privileged actions like user management and analytics.
     */
    @Override
    public void displayDashboard() {
        System.out.println("\n=== SYSTEM ADMIN DASHBOARD ===");
        System.out.println("Welcome, Administrator " + getFullName() + "!");
        System.out.println("1. Manage Flights");
        System.out.println("2. Manage Users");
        System.out.println("3. View All Bookings");
        System.out.println("4. Platform Analytics");
        System.out.println("5. Manage Airports");
    }
}
