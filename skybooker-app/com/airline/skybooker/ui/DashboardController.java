package com.airline.skybooker.ui;

import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.managers.PriorityBookingManager;
import java.util.Scanner;

/**
 * Acts as the Front Controller for authenticated users.
 * Routes dashboard selections to the appropriate UI module, 
 * keeping Main.java clean and compliant with SRP.
 */
public class DashboardController {
    
    private final Scanner scanner;
    private final ProfileUI profileUI;
    private final BookingManagementUI bookingManagementUI;
    private final FlightSearchUI searchUI;

    public DashboardController(Scanner scanner, ProfileUI profileUI, BookingManagementUI bookingManagementUI, FlightSearchUI searchUI) {
        this.scanner = scanner;
        this.profileUI = profileUI;
        this.bookingManagementUI = bookingManagementUI;
        this.searchUI = searchUI;
    }

    public void startLoop(User user) {
        boolean running = true;
        
        while (running) {
            user.displayDashboard();
            System.out.println("4. Search Flights (Book a Ticket)");
            System.out.println("5. Exit Application");
            System.out.println("9. [ADMIN] Generate Processing Report");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            running = routeChoice(choice, user);
        }
    }

    private boolean routeChoice(String choice, User user) {
        // If not a passenger, they can only search flights or exit for now
        boolean isPassenger = user instanceof Passenger;
        Passenger passenger = isPassenger ? (Passenger) user : null;

        switch (choice) {
            case "1":
                if (isPassenger) profileUI.handleViewProfile(passenger);
                else System.out.println("Feature coming soon!");
                return true;
            case "2":
                if (isPassenger) bookingManagementUI.displayMyBookings(passenger);
                else System.out.println("Feature coming soon!");
                return true;
            case "3":
                if (isPassenger) profileUI.handleProfileUpdate(passenger);
                else System.out.println("Feature coming soon!");
                return true;
            case "4":
                searchUI.startSearchFlow();
                return true;
            case "5":
                System.out.println("Thank you for using SkyBooker! Goodbye.");
                return false;
            case "9":
                PriorityBookingManager.getInstance().generateProcessingReport();
                return true;
            default:
                System.out.println("Invalid choice. Please try again.");
                return true;
        }
    }
}
