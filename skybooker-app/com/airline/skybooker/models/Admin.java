package com.airline.skybooker.models;

import com.airline.skybooker.enums.Role;

/**
 * Admin role extending User.
 * Demonstrates OOAD Inheritance and Polymorphism.
 */
public class Admin extends User {

    public Admin(int userId, String fullName, String email, String passwordHash, String phone) {
        super(userId, fullName, email, passwordHash, phone, Role.ADMIN);
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n=== SYSTEM ADMIN DASHBOARD ===");
        System.out.println("Welcome, Administrator " + getFullName() + "!");
        System.out.println("1. Manage Users");
        System.out.println("2. View All Bookings");
        System.out.println("3. Platform Analytics");
    }
}
