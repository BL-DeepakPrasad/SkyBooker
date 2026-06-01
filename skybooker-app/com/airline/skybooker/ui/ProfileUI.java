package com.airline.skybooker.ui;

import com.airline.skybooker.models.Passenger;
import java.util.Scanner;

/**
 * Command-line interface for managing and updating passenger profiles.
 * Facilitates the viewing and modification of personal details and notification preferences.
 */
public class ProfileUI {
    private final Scanner scanner;

    /**
     * Constructs the profile management interface with the provided input scanner.
     *
     * @param scanner the input reader for capturing passenger inputs
     */
    public ProfileUI(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Displays the passenger's current profile details and presents options for inline modifications.
     *
     * @param passenger the authenticated passenger whose profile is being viewed
     */
    public void handleViewProfile(Passenger passenger) {
        System.out.println("\n============================================");
        System.out.println("               USER PROFILE                 ");
        System.out.println("============================================");
        System.out.println("Name:        " + passenger.getFullName());
        System.out.println("Email:       " + passenger.getEmail());
        System.out.println("Phone:       " + passenger.getPhone());
        System.out.println("Nationality: " + passenger.getNationality());
        System.out.println("Passport:    " + passenger.getPassportNumber());
        System.out.println("Role:        " + passenger.getRole());
        System.out.println("WhatsApp:    " + (passenger.isWhatsappOptIn() ? "Enabled" : "Disabled"));
        System.out.println("============================================");
        System.out.println("1. Update Phone");
        System.out.println("2. Update Passport");
        System.out.println("3. Update Password");
        System.out.println("4. Toggle WhatsApp Alerts");
        System.out.println("0. Cancel");
        System.out.print("Enter choice: ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            System.out.print("Enter new phone number: ");
            passenger.setPhone(scanner.nextLine().trim());
            System.out.println("Phone updated successfully!");
        } else if (choice.equals("2")) {
            System.out.print("Enter new passport number: ");
            passenger.setPassportNumber(scanner.nextLine().trim());
            System.out.println("Passport updated successfully!");
        } else if (choice.equals("3")) {
            System.out.println("Password update flow coming soon.");
        } else if (choice.equals("4")) {
            boolean current = passenger.isWhatsappOptIn();
            passenger.setWhatsappOptIn(!current);
            System.out.println("WhatsApp Alerts are now " + (!current ? "ON" : "OFF") + ".");
        }
        System.out.println("Press Enter to return to Dashboard...");
        scanner.nextLine();
    }

    /**
     * Prompts the passenger to update their contact information and travel documents through an interactive wizard.
     *
     * @param passenger the authenticated passenger updating their details
     */
    public void handleProfileUpdate(Passenger passenger) {
        System.out.println("\n--- UPDATE PROFILE ---");
        System.out.println("Leave blank to keep current value.");
        
        System.out.print("New Phone Number [" + passenger.getPhone() + "]: ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty()) passenger.setPhone(phone);

        System.out.print("New Passport Number [" + passenger.getPassportNumber() + "]: ");
        String passport = scanner.nextLine().trim();
        if (!passport.isEmpty()) passenger.setPassportNumber(passport);

        System.out.print("New Nationality [" + passenger.getNationality() + "]: ");
        String nationality = scanner.nextLine().trim();
        if (!nationality.isEmpty()) passenger.setNationality(nationality);

        System.out.println("Profile updated successfully!");
    }
}
