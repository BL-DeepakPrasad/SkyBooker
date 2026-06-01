package com.airline.skybooker.ui;

import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.models.AirlineStaff;
import com.airline.skybooker.models.Admin;
import com.airline.skybooker.managers.PriorityBookingManager;
import java.util.Scanner;

/**
 * Acts as the Front Controller for authenticated users.
 * Routes dashboard selections to the appropriate controller. 
 * keeping Main.java clean and compliant with SRP.
 */
public class DashboardController {
    
    private final Scanner scanner;
    private final ProfileUI profileUI;
    private final BookingManagementUI bookingManagementUI;
    private final FlightSearchUI searchUI;
    private final AdminFlightUI adminFlightUI;
    private final AdminAirportUI adminAirportUI;
    private final AdminAnalyticsUI adminAnalyticsUI;
    private final CheckInUI checkInUI;

    public DashboardController(Scanner scanner, ProfileUI profileUI, BookingManagementUI bookingManagementUI, 
                               FlightSearchUI searchUI, AdminFlightUI adminFlightUI, 
                               AdminAirportUI adminAirportUI, AdminAnalyticsUI adminAnalyticsUI,
                               CheckInUI checkInUI) {
        this.scanner = scanner;
        this.profileUI = profileUI;
        this.bookingManagementUI = bookingManagementUI;
        this.searchUI = searchUI;
        this.adminFlightUI = adminFlightUI;
        this.adminAirportUI = adminAirportUI;
        this.adminAnalyticsUI = adminAnalyticsUI;
        this.checkInUI = checkInUI;
    }

    public void startLoop(User user) {
        boolean running = true;
        
        while (running) {
            user.displayDashboard();
            
            if (user instanceof Passenger) {
                System.out.println("4. Search Flights or Book a Ticket ");
                System.out.println("5. Web Check-in (Get Boarding Pass)");
            }
            if (user instanceof Admin) {
                System.out.println("9. Generate Processing Report");
            }
            
            System.out.println("0. Logout / Exit");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            running = routeChoice(choice, user);
        }
    }

    private boolean routeChoice(String choice, User user) {
        boolean isPassenger = user instanceof Passenger;
        boolean isStaff = user instanceof AirlineStaff;
        boolean isAdmin = user instanceof Admin;
        Passenger passenger = isPassenger ? (Passenger) user : null;

        switch (choice) {
            case "1":
                if (isPassenger) profileUI.handleViewProfile(passenger);
                else if (isStaff || isAdmin) adminFlightUI.startAdminFlow();
                return true;
            case "2":
                if (isPassenger) bookingManagementUI.displayMyBookings(passenger);
                else if (isAdmin) System.out.println("User Management coming soon!");
                else System.out.println("Feature coming soon!");
                return true;
            case "3":
                if (isPassenger) profileUI.handleProfileUpdate(passenger);
                else if (isAdmin) System.out.println("View Bookings coming soon!");
                else System.out.println("Feature coming soon!");
                return true;
            case "4":
                if (isPassenger) searchUI.startSearchFlow();
                else if (isAdmin) adminAnalyticsUI.startAnalyticsFlow();
                else System.out.println("Invalid choice.");
                return true;
            case "5":
                if (isPassenger) checkInUI.startCheckInFlow();
                else if (isAdmin) adminAirportUI.startAirportFlow();
                else System.out.println("Invalid choice.");
                return true;
            case "9":
                if (isAdmin) PriorityBookingManager.getInstance().generateProcessingReport();
                else System.out.println("Invalid choice.");
                return true;
            case "0":
                System.out.println("Logging out... Goodbye!");
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
                return true;
        }
    }
}
