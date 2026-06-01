package com.airline.skybooker.ui;

import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.models.AirlineStaff;
import java.util.Scanner;
import java.util.Optional;
import java.util.List;

/**
 * Command-line interface for administrator oversight of user accounts.
 * Manages user roles, operational statuses, and account retrieval.
 */
public class AdminUserUI {
    private final Scanner scanner;
    private final AuthenticationManager authManager;

    /**
     * Constructs the user administration interface with the provided input scanner.
     *
     * @param scanner the input reader for capturing administrator commands
     */
    public AdminUserUI(Scanner scanner) {
        this.scanner = scanner;
        this.authManager = AuthenticationManager.getInstance();
    }

    /**
     * Initiates the main interactive loop for user management operations.
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
     * Retrieves and lists summaries of all registered users in the system.
     */
    private void handleViewAllUsers() {
        List<User> users = authManager.getAllUsers();
        System.out.println("\n--- ALL REGISTERED USERS ---");
        for (User u : users) {
            printUserSummary(u);
        }
    }

    /**
     * Looks up a specific user account using their registered email address.
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
     * Switches a specified user account's operational state between active and suspended.
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
     * Upgrades a standard passenger account to an airline staff role with elevated privileges.
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
     * Formats and prints essential account details for a given user.
     *
     * @param u the user domain model to summarize
     */
    private void printUserSummary(User u) {
        String type = u.getClass().getSimpleName();
        String status = u.isActive() ? "Active" : "Suspended";
        System.out.printf("[%d] %s (%s) - Role: %s - Status: %s%n", 
            u.getUserId(), u.getFullName(), u.getEmail(), type, status);
    }
}
