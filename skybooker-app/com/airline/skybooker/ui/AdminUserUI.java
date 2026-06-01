package com.airline.skybooker.ui;

import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.models.AirlineStaff;
import java.util.Scanner;
import java.util.Optional;
import java.util.List;

public class AdminUserUI {
    private final Scanner scanner;
    private final AuthenticationManager authManager;

    public AdminUserUI(Scanner scanner) {
        this.scanner = scanner;
        this.authManager = AuthenticationManager.getInstance();
    }

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

    private void handleViewAllUsers() {
        List<User> users = authManager.getAllUsers();
        System.out.println("\n--- ALL REGISTERED USERS ---");
        for (User u : users) {
            printUserSummary(u);
        }
    }

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

    private void printUserSummary(User u) {
        String type = u.getClass().getSimpleName();
        String status = u.isActive() ? "Active" : "Suspended";
        System.out.printf("[%d] %s (%s) - Role: %s - Status: %s%n", 
            u.getUserId(), u.getFullName(), u.getEmail(), type, status);
    }
}
