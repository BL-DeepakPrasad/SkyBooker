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
 * Main application entry point.
 * Acts as the Front Controller, delegating logic to UI classes to preserve SRP.
 */
public class Main {
    private final Scanner scanner;
    private final AuthenticationManager authManager;
    
    // UI Controllers
    private final AuthUI authUI;
    private final FlightSearchUI searchUI;
    private final DashboardController dashboardController;

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
     * Bootstraps the application.
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

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
}