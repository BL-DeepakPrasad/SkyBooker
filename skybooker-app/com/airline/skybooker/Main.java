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
        BookingManagementUI bookingManagementUI = new BookingManagementUI(scanner);
        
        this.dashboardController = new DashboardController(scanner, profileUI, bookingManagementUI, searchUI);
    }

    /**
     * Bootstraps the application.
     */
    public void start() {
        System.out.println("=== WELCOME TO SKYBOOKER ===");
        
        // authentication
        authUI.displayAuthMenu();

        // register user
        if (authManager.getCurrentUser().isPresent()) {
            User user = authManager.getCurrentUser().get();
            dashboardController.startLoop(user);
        } else {
           // Guest
            searchUI.startSearchFlow();
        }
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
}