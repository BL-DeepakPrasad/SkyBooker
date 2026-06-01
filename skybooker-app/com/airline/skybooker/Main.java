package com.airline.skybooker;

import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.User;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.ui.AuthUI;
import com.airline.skybooker.ui.ProfileUI;
import com.airline.skybooker.ui.FlightSearchUI;
import com.airline.skybooker.ui.BookingUI;
import com.airline.skybooker.ui.BookingManagementUI;
import com.airline.skybooker.ui.DashboardController;
import com.airline.skybooker.ui.AdminFlightUI;
import com.airline.skybooker.ui.AdminAirportUI;
import com.airline.skybooker.ui.AdminAnalyticsUI;
import com.airline.skybooker.ui.CheckInUI;
import com.airline.skybooker.ui.AdminUserUI;
import com.airline.skybooker.exception.DatabaseConnectionException;
import com.airline.skybooker.utils.ErrorLogger;

import java.util.Scanner;

/**
 * Primary bootstrapping entry point for the SkyBooker application layer.
 * Implements the Front Controller design pattern to intercept and route user interactions to specialized UI controllers, preserving Single Responsibility Principle (SRP) throughout the architectural boundary.
 */
public class Main {
    private final Scanner scanner;
    private final AuthenticationManager authManager;
    
    // UI Controllers
    private final AuthUI authUI;
    private final FlightSearchUI searchUI;
    private final DashboardController dashboardController;

    /**
     * Initializes core infrastructure components and binds UI controllers to their required business services.
     */
    public Main() {
        this.scanner = new Scanner(System.in);
        this.authManager = AuthenticationManager.getInstance();

        this.authUI = new AuthUI(scanner);
        ProfileUI profileUI = new ProfileUI(scanner);
        SeatService seatService = new SeatService();
        BookingUI bookingUI = new BookingUI(scanner, seatService);
        this.searchUI = new FlightSearchUI(scanner, bookingUI);
        BookingManagementUI bookingManagementUI = new BookingManagementUI(scanner, seatService);
        AdminFlightUI adminFlightUI = new AdminFlightUI(scanner, seatService);
        AdminAirportUI adminAirportUI = new AdminAirportUI(scanner);
        AdminAnalyticsUI adminAnalyticsUI = new AdminAnalyticsUI(scanner);
        CheckInUI checkInUI = new CheckInUI(scanner);
        AdminUserUI adminUserUI = new AdminUserUI(scanner);
        
        this.dashboardController = new DashboardController(scanner, profileUI, bookingManagementUI, searchUI, adminFlightUI, adminAirportUI, adminAnalyticsUI, checkInUI, adminUserUI);
    }

    /**
     * Initializes the interactive application loop.
     * Evaluates initial hardware and database readiness before entering the primary authentication and routing cycles.
     */
    public void start() {
        System.out.println("=== WELCOME TO SKYBOOKER ===");
        
        // Simulate Database Connection Attempt (5% chance of failure)
        try {
            if (Math.random() < 0.05) {
                throw new DatabaseConnectionException("Failed to connect to primary RDS cluster.");
            }
            System.out.println("[SYSTEM] Database connected successfully.");
        } catch (DatabaseConnectionException e) {
            System.out.println("[SYSTEM FATAL] Cannot boot application: " + e.getMessage());
            ErrorLogger.logError(e);
            return;
        }
        
        while (true) {
            // authentication
            if (!authUI.displayAuthMenu()) {
                System.out.println("Exiting Skybooker. Goodbye!");
                break;
            }

            // run the guest
            if (authManager.getCurrentUser().isPresent()) {
                User user = authManager.getCurrentUser().get();
                dashboardController.startLoop(user);
                // Clear session when dashboard loop exits
                authManager.logout();
            } else {
               // Guest
                searchUI.startSearchFlow();
            }
        }
    }

    /**
     * Standard Java entry method allocating the process context and triggering the primary initialization sequence.
     * 
     * @param args Command-line arguments supplied by the runtime environment
     */
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
}