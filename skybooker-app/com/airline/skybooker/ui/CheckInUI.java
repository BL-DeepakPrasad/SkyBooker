package com.airline.skybooker.ui;

import com.airline.skybooker.managers.CheckInManager;
import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.managers.NotificationManager;
import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.BoardingPass;

import java.util.Scanner;

public class CheckInUI {

    private final Scanner scanner;
    private final CheckInManager checkInManager;

    public CheckInUI(Scanner scanner) {
        this.scanner = scanner;
        this.checkInManager = CheckInManager.getInstance();
    }

    public void startCheckInFlow() {
        System.out.println("\n=== WEB CHECK-IN ===");
        User currentUser = AuthenticationManager.getInstance().getCurrentUser().orElse(null);
        if (currentUser == null) {
            System.out.println("Please login to check-in.");
            return;
        }

        System.out.print("Enter your Booking PNR: ");
        String pnr = scanner.nextLine().trim();

        try {
            Booking booking = checkInManager.validateAndRetrieveBooking(pnr, currentUser);
            Flight flight = FlightManager.getInstance().getAllFlights().stream()
                    .filter(f -> f.getFlightId() == booking.getFlightId())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Flight not found."));

            System.out.println("\n[1/3] Passenger Details & Documents Verified ✓");
            
            System.out.println("\n[2/3] Travel Preferences");
            System.out.print("Do you require Special Assistance (Wheelchair, etc.)? (y/n): ");
            boolean specialAssistance = scanner.nextLine().trim().equalsIgnoreCase("y");

            System.out.print("Enter expected checked baggage (e.g., '1 Bag 20kg', 'None'): ");
            String baggage = scanner.nextLine().trim();

            System.out.println("\n[3/3] Seat Confirmation");
            System.out.println("Your currently assigned seat is: " + booking.getSeatNumber());
            System.out.print("Would you like to keep this seat? (y/n): ");
            String keepSeat = scanner.nextLine().trim();
            String finalSeat = booking.getSeatNumber();

            if (keepSeat.equalsIgnoreCase("n")) {
                System.out.println("Seat change requested. Note: Changing seats via Web UI is mocked here.");
                System.out.print("Enter new seat (e.g. 14B): ");
                finalSeat = scanner.nextLine().trim();
            }

            System.out.println("\nFinalizing Check-in...");
            BoardingPass pass = checkInManager.completeCheckIn(booking, flight, currentUser, finalSeat, baggage, specialAssistance);

            System.out.println("\n[SUCCESS] You are successfully checked in!");
            System.out.println("Your booking state is now: " + booking.getStatus());
            
            System.out.println("\nWould you like to print/view your Boarding Pass now? (y/n)");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println(pass.getFormattedPass());
            }

            System.out.println("Downloading Boarding Pass to local file...");
            pass.downloadToFile();

            System.out.println("Sending Boarding Pass via Email...");
            NotificationManager.getInstance().sendBoardingPass(currentUser, pass);

        } catch (Exception e) {
            System.out.println("[CHECK-IN FAILED] " + e.getMessage());
        }
    }
}
