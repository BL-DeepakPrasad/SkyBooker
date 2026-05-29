package com.airline.skybooker;

import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.ui.AuthUI;
import com.airline.skybooker.ui.ProfileUI;
import com.airline.skybooker.ui.FlightSearchUI;
import com.airline.skybooker.ui.BookingUI;
import com.airline.skybooker.ui.BookingManagementUI;

import java.util.Scanner;

/**
 * Main application entry point for the Flight Search module.
 * Acts as the Front Controller, delegating logic to UI classes to preserve SRP.
 */
public class Main {
    private final Scanner scanner;
    private final AuthenticationManager authManager;
    
    // UI Modules
    private final AuthUI authUI;
    private final ProfileUI profileUI;
    private final FlightSearchUI searchUI;
    private final BookingManagementUI bookingManagementUI;

    public Main() {
        this.scanner = new Scanner(System.in);
        this.authManager = AuthenticationManager.getInstance();
        
        // Instantiate UI dependencies
        this.authUI = new AuthUI(scanner);
        this.profileUI = new ProfileUI(scanner);
        SeatService seatService = new SeatService();
        BookingUI bookingUI = new BookingUI(scanner, seatService);
        this.searchUI = new FlightSearchUI(scanner, bookingUI);
        this.bookingManagementUI = new BookingManagementUI(scanner);
    }

    /**
     * Bootstraps the application.
     */
    public void start() {
        System.out.println("=== WELCOME TO SKYBOOKER ===");
        
        // 1. Authentication Phase
        authUI.displayAuthMenu();

        // 2. Main Application Loop
        if (authManager.getCurrentUser().isPresent()) {
            User user = authManager.getCurrentUser().get();
            boolean running = true;
            
            while (running) {
                user.displayDashboard();
                System.out.println("4. Search Flights (Book a Ticket)");
                System.out.println("5. Exit Application");
                System.out.print("Enter choice: ");
                String dashChoice = scanner.nextLine().trim();

                if (dashChoice.equals("1") && user instanceof Passenger) {
                    profileUI.handleViewProfile((Passenger) user);
                } else if (dashChoice.equals("2") && user instanceof Passenger) {
                    bookingManagementUI.displayMyBookings((Passenger) user);
                } else if (dashChoice.equals("3") && user instanceof Passenger) {
                    profileUI.handleProfileUpdate((Passenger) user);
                } else if (dashChoice.equals("4")) {
                    searchUI.startSearchFlow();
                } else if (dashChoice.equals("5")) {
                    System.out.println("Thank you for using SkyBooker! Goodbye.");
                    running = false;
                } else {
                    System.out.println("Invalid choice or Feature coming soon!");
                }
            }
        } else {
            // Guest Flow
            searchUI.startSearchFlow();
        }
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
}