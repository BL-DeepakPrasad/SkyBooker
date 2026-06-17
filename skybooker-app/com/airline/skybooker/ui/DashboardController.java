package com.airline.skybooker.ui;

import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.models.AirlineStaff;
import com.airline.skybooker.models.Admin;
import com.airline.skybooker.managers.PriorityBookingManager;
import java.util.Scanner;

/**
 * The main menu that appears after a user logs in.
 * It checks whether the user is a normal passenger, a staff member, or an admin, 
 * and shows them the correct options for their role.
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
    private final AdminUserUI adminUserUI;
    private final StaffOperationsUI staffOperationsUI;

    /**
     * Sets up the dashboard by collecting all the different menus the user might need to visit.
     *
     * @param scanner reads text typed by the user in the console
     * @param profileUI menu for editing user profiles
     * @param bookingManagementUI menu for managing booked tickets
     * @param searchUI menu for finding new flights
     * @param adminFlightUI menu for admins to manage flights
     * @param adminAirportUI menu for admins to manage airports
     * @param adminAnalyticsUI menu for admins to see reports
     * @param checkInUI menu for checking in to a flight
     * @param adminUserUI menu for admins to manage users
     * @param staffOperationsUI menu for staff to see flight details
     */
    public DashboardController(Scanner scanner, ProfileUI profileUI, BookingManagementUI bookingManagementUI, 
                               FlightSearchUI searchUI, AdminFlightUI adminFlightUI, 
                               AdminAirportUI adminAirportUI, AdminAnalyticsUI adminAnalyticsUI,
                               CheckInUI checkInUI, AdminUserUI adminUserUI,
                               StaffOperationsUI staffOperationsUI) {
        this.scanner = scanner;
        this.profileUI = profileUI;
        this.bookingManagementUI = bookingManagementUI;
        this.searchUI = searchUI;
        this.adminFlightUI = adminFlightUI;
        this.adminAirportUI = adminAirportUI;
        this.adminAnalyticsUI = adminAnalyticsUI;
        this.checkInUI = checkInUI;
        this.adminUserUI = adminUserUI;
        this.staffOperationsUI = staffOperationsUI;
    }

    /**
     * Keeps showing the main menu to the user until they decide to log out.
     * The menu choices look different depending on whether the user is a passenger or an admin.
     *
     * @param user the person currently logged into the app
     */
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

    /**
     * Takes the number the user typed in the menu and sends them to the correct screen.
     *
     * @param choice the number chosen from the menu
     * @param user the person making the choice
     * @return true if the menu should stay open, false if the user wants to log out
     */
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
                else if (isStaff) staffOperationsUI.handleViewManifest();
                else if (isAdmin) adminUserUI.startUserManagementFlow();
                else System.out.println("Feature coming soon!");
                return true;
            case "3":
                if (isPassenger) profileUI.handleProfileUpdate(passenger);
                else if (isStaff) staffOperationsUI.handleCrewSchedules();
                else if (isAdmin) bookingManagementUI.displayAllSystemBookings((Admin) user);
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
