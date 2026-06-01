package com.airline.skybooker.ui;

import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.utils.ValidationUtils;
import com.airline.skybooker.utils.InputReader;
import java.util.Scanner;

/**
 * Command-line interface for user authentication and registration workflows.
 * Guides users through login, signup, or continuing as a guest.
 */
public class AuthUI {
    private final AuthenticationManager authManager;
    private final Scanner scanner;

    /**
     * Constructs the authentication interface with the provided input scanner.
     *
     * @param scanner the input reader for capturing user credentials
     */
    public AuthUI(Scanner scanner) {
        this.scanner = scanner;
        this.authManager = AuthenticationManager.getInstance();
    }

    /**
     * Displays the initial authentication menu and loops until a valid session is established or the user exits.
     *
     * @return true if the application should proceed, false if the user chose to exit
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
     * Prompts for login credentials and delegates authentication to the underlying manager.
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
     * Gathers and validates required details to register a new passenger account.
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
