package com.airline.skybooker.models;

import com.airline.skybooker.enums.Role;

/**
 * Administrator user in the system with special privileges to configure flights and manage other users.
 * This class exists to differentiate normal passengers from system managers, ensuring secure access to administrative tools.
 * It extends the base User class to inherit common profile details like name and email.
 */
public class Admin extends User {

    /**
     * Creates a new admin account with their contact and login details.
     * We automatically assign the ADMIN role here because anyone created using this class is definitively an administrator.
     *
     * @param userId       unique ID number for this admin
     * @param fullName     their full real name
     * @param email        email address used for logging in
     * @param passwordHash scrambled version of their password for security
     * @param phone        contact phone number
     */
    public Admin(int userId, String fullName, String email, String passwordHash, String phone) {
        super(userId, fullName, email, passwordHash, phone, Role.ADMIN);
    }

    /**
     * Shows the admin-specific menu on the screen.
     * We override this method from the base User class so that admins see a completely different set of options (like managing flights) compared to a regular passenger.
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
