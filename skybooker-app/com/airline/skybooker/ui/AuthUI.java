package com.airline.skybooker.ui;

import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.utils.ValidationUtils;
import com.airline.skybooker.utils.InputReader;
import java.util.Scanner;

/**
 * Handles the first menu that users see when they open the application.
 * It lets users log into their existing account, create a new one, 
 * or just browse as a guest without signing up.
 */
public class AuthUI {
    private final AuthenticationManager authManager;
    private final Scanner scanner;

    /**
     * Sets up the authentication menu using a Scanner for reading user input.
     *
     * @param scanner reads text typed by the user in the console
     */
    public AuthUI(Scanner scanner) {
        this.scanner = scanner;
        this.authManager = AuthenticationManager.getInstance();
    }

    /**
     * Shows the login and registration menu and keeps asking until the user logs in, signs up, or leaves.
     *
     * @return true if the user decides to enter the app, false if they decide to exit completely
     */
    public boolean displayAuthMenu() {
        while (authManager.getCurrentUser().isEmpty()) {
            System.out.println("\n1. Login");
            System.out.println("2. Register as Passenger");
            System.out.println("3. Continue as Guest");
            System.out.println("4. Exit Application");
            System.out.print("Enter choice (1-4): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                handleLogin();
            } else if (choice.equals("2")) {
                handleRegistration();
            } else if (choice.equals("3")) {
                return true;
            } else if (choice.equals("4")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Asks the user for their email and password, then checks if they match an existing account.
     * If they match, the user is logged in.
     */
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

    /**
     * Asks a new user for their personal details like name, email, and password to create an account.
     * It ensures everything is formatted correctly before saving.
     */
    private void handleRegistration() {
        try {
            String name = InputReader.readString(scanner, "Full Name: ", ValidationUtils::validateName);
            String email = InputReader.readString(scanner, "Email: ", ValidationUtils::validateEmail);
            String pass = InputReader.readString(scanner, "Password: ", ValidationUtils::validatePassword);
            String phone = InputReader.readString(scanner, "Phone: ", ValidationUtils::validatePhone);
            String passport = InputReader.readString(scanner, "Passport Number: ", ValidationUtils::validatePassport);
            
            System.out.print("Nationality: ");
            String nationality = scanner.nextLine().trim();

            authManager.registerPassenger(name, email, pass, phone, passport, nationality);
            System.out.println("Registration Successful! Please login.");
        } catch (Exception e) {
            System.out.println("Registration Failed: " + e.getMessage());
        }
    }
}
