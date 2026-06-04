package com.airline.skybooker.ui;

import com.airline.skybooker.models.Passenger;
import java.util.Scanner;

/**
 * Provides a menu where passengers can view and change their account details.
 * This includes things like their phone number, passport details, or turning on WhatsApp alerts.
 */
public class ProfileUI {
    private final Scanner scanner;

    /**
     * Sets up the profile menu using a Scanner for reading user input.
     *
     * @param scanner reads text typed by the user in the console
     */
    public ProfileUI(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Prints out all the personal info saved for the user and lets them change specific details one by one.
     *
     * @param passenger the user looking at their profile
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
     * A quick wizard that asks the user if they want to update their phone, passport, or nationality.
     * Leaving a field blank keeps the old information.
     *
     * @param passenger the user updating their details
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
