package com.airline.skybooker.ui;

import com.airline.skybooker.managers.AuthenticationManager;
import java.util.Scanner;

public class AuthUI {
    private final AuthenticationManager authManager;
    private final Scanner scanner;

    public AuthUI(Scanner scanner) {
        this.scanner = scanner;
        this.authManager = AuthenticationManager.getInstance();
    }

    public void displayAuthMenu() {
        while (authManager.getCurrentUser().isEmpty()) {
            System.out.println("\n1. Login");
            System.out.println("2. Register as Passenger");
            System.out.println("3. Continue as Guest");
            System.out.print("Enter choice (1-3): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                handleLogin();
            } else if (choice.equals("2")) {
                handleRegistration();
            } else if (choice.equals("3")) {
                break;
            }
        }
    }

    private void handleLogin() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();
        
        if (authManager.login(email, pass)) {
            System.out.println("Login Successful!");
        } else {
            System.out.println(" Invalid credentials.");
        }
    }

    private void handleRegistration() {
        try {
            System.out.print("Full Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password: ");
            String pass = scanner.nextLine().trim();
            System.out.print("Phone: ");
            String phone = scanner.nextLine().trim();
            System.out.print("Passport Number: ");
            String passport = scanner.nextLine().trim();
            System.out.print("Nationality: ");
            String nationality = scanner.nextLine().trim();

            authManager.registerPassenger(name, email, pass, phone, passport, nationality);
            System.out.println("Registration Successful! Please login.");
        } catch (Exception e) {
            System.out.println("Registration Failed: " + e.getMessage());
        }
    }
}
