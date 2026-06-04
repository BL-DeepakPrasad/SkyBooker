package com.airline.skybooker;

import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.User;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.ui.AuthUI;
import com.airline.skybooker.ui.StaffOperationsUI;
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
 * Starts the SkyBooker application.
 * This class acts as the main hub, setting up all the user interfaces (UIs) and services 
 * so the app can talk to the user and handle their requests.
 */
public class Main {
    private final Scanner scanner;
    private final AuthenticationManager authManager;
    
    // UI Controllers
    private final AuthUI authUI;
    private final FlightSearchUI searchUI;
    private final DashboardController dashboardController;

    /**
     * Prepares all the tools, screens, and services the app needs to run.
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
        CheckInUI checkInUI = new CheckInUI(scanner, seatService);
        AdminUserUI adminUserUI = new AdminUserUI(scanner);
        StaffOperationsUI staffOperationsUI = new StaffOperationsUI(scanner);
        
        this.dashboardController = new DashboardController(scanner, profileUI, bookingManagementUI, searchUI, adminFlightUI, adminAirportUI, adminAnalyticsUI, checkInUI, adminUserUI, staffOperationsUI);
    }

    /**
     * Starts the main menu loop.
     * It handles logging in users and showing them the right dashboard depending on if they are logged in or a guest.
     */
    public void start() {
        System.out.println("=== WELCOME TO SKYBOOKER ===");
        
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
     * The very first code that runs when you launch the application.
     * 
     * @param args command-line arguments (not used here)
     */
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
}