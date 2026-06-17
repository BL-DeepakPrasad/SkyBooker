package com.airline.skybooker.ui;

import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.models.AirlineStaff;
import java.util.Scanner;
import java.util.Optional;
import java.util.List;

/**
 * Provides a menu for admins to manage all user accounts in the system.
 * This class allows admins to see all users, suspend accounts if there are issues,
 * and promote a regular passenger to a staff member.
 */
public class AdminUserUI {
    private final Scanner scanner;
    private final AuthenticationManager authManager;

    /**
     * Sets up the user management menu using a Scanner for reading user input.
     *
     * @param scanner reads text typed by the admin in the console
     */
    public AdminUserUI(Scanner scanner) {
        this.scanner = scanner;
        this.authManager = AuthenticationManager.getInstance();
    }

    /**
     * Shows the main user management menu and keeps it running in a loop.
     * Directs the admin to specific tasks based on what they type.
     */
    public void startUserManagementFlow() {
        while (true) {
            System.out.println("\n=== USER MANAGEMENT (ADMIN) ===");
            System.out.println("1. View All Users");
            System.out.println("2. Search User by Email");
            System.out.println("3. Suspend/Activate User Account");
            System.out.println("4. Promote Passenger to Airline Staff");
            System.out.println("0. Return to Dashboard");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                break;
            } else if (choice.equals("1")) {
                handleViewAllUsers();
            } else if (choice.equals("2")) {
                handleSearchUser();
            } else if (choice.equals("3")) {
                handleToggleStatus();
            } else if (choice.equals("4")) {
                handlePromoteUser();
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    /**
     * Prints a short summary of every user registered in the system.
     * This helps the admin quickly see who is on the platform.
     */
    private void handleViewAllUsers() {
        List<User> users = authManager.getAllUsers();
        System.out.println("\n--- ALL REGISTERED USERS ---");
        for (User u : users) {
            printUserSummary(u);
        }
    }

    /**
     * Asks the admin for an email address and searches for the matching user account.
     * This is an easy way to locate a specific person's profile.
     */
    private void handleSearchUser() {
        System.out.print("\nEnter Email to Search: ");
        String email = scanner.nextLine().trim();
        Optional<User> userOpt = authManager.getUserByEmail(email);
        
        if (userOpt.isPresent()) {
            System.out.println("\nUser Found:");
            printUserSummary(userOpt.get());
        } else {
            System.out.println("No user found with email: " + email);
        }
    }

    /**
     * Suspends or activates a user's account. 
     * Suspending prevents the user from logging in, which is useful if they break the rules.
     */
    private void handleToggleStatus() {
        System.out.print("\nEnter User ID to modify status: ");
        try {
            int userId = Integer.parseInt(scanner.nextLine().trim());
            Optional<User> userOpt = authManager.getUserById(userId);
            if (userOpt.isPresent()) {
                User u = userOpt.get();
                boolean newStatus = !u.isActive();
                authManager.toggleUserStatus(userId, newStatus);
                System.out.println("[SUCCESS] User " + u.getFullName() + " is now " + (newStatus ? "ACTIVE" : "SUSPENDED"));
            } else {
                System.out.println("[FAILED] User ID not found.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. " + e.getMessage());
        }
    }

    /**
     * Upgrades a normal passenger account so they become airline staff.
     * Staff members have extra permissions like managing flights.
     */
    private void handlePromoteUser() {
        System.out.print("\nEnter Passenger ID to Promote: ");
        try {
            int userId = Integer.parseInt(scanner.nextLine().trim());
            Optional<User> userOpt = authManager.getUserById(userId);
            
            if (userOpt.isPresent() && userOpt.get() instanceof Passenger) {
                boolean success = authManager.promoteToAirlineStaff(userId);
                if (success) {
                    System.out.println("[SUCCESS] " + userOpt.get().getFullName() + " has been promoted to Airline Staff.");
                } else {
                    System.out.println("[FAILED] Could not promote user.");
                }
            } else {
                System.out.println("[FAILED] User not found or is not a Passenger.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. " + e.getMessage());
        }
    }

    /**
     * Prints a quick one-line summary of a user's details, such as their ID, name, role, and status.
     *
     * @param u the user we want to print details for
     */
    private void printUserSummary(User u) {
        String type = u.getClass().getSimpleName();
        String status = u.isActive() ? "Active" : "Suspended";
        System.out.printf("[%d] %s (%s) - Role: %s - Status: %s%n", 
            u.getUserId(), u.getFullName(), u.getEmail(), type, status);
    }
}
