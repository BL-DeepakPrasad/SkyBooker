package com.airline.skybooker.ui;

import com.airline.skybooker.models.Passenger;
import java.util.Scanner;

public class ProfileUI {
    private final Scanner scanner;

    public ProfileUI(Scanner scanner) {
        this.scanner = scanner;
    }

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
        System.out.println("============================================");
        System.out.println("Press Enter to return to Dashboard...");
        scanner.nextLine();
    }

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
