package com.airline.skybooker.models;

import com.airline.skybooker.enums.Role;

/**
 * Employee of a specific airline using the platform.
 * This class exists to give airline workers special permissions to manage their own flights and passengers, without giving them full system admin rights.
 * It extends the base User class because a staff member is still a user who logs into the system.
 */
public class AirlineStaff extends User {

    /**
     * Creates a new staff account with their personal and login details.
     * We automatically assign the AIRLINE_STAFF role so the system knows to grant them access to airline-specific tools.
     *
     * @param userId       unique ID number for the employee
     * @param fullName     their full real name
     * @param email        email address used for logging in
     * @param passwordHash scrambled version of their password for security
     * @param phone        contact phone number
     */
    public AirlineStaff(int userId, String fullName, String email, String passwordHash, String phone) {
        super(userId, fullName, email, passwordHash, phone, Role.AIRLINE_STAFF);
    }

    /**
     * Shows the staff-specific menu on the screen.
     * We override this method from the User class to display options relevant only to airline workers, like viewing passenger manifests.
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
