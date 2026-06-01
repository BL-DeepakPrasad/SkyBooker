package com.airline.skybooker.ui;

import com.airline.skybooker.managers.CheckInManager;
import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.managers.NotificationManager;
import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.BoardingPass;
import com.airline.skybooker.exception.BookingNotFoundException;
import com.airline.skybooker.utils.ErrorLogger;

import java.util.Scanner;

/**
 * Command-line interface for the web check-in workflow.
 * Verifies booking eligibility, confirms seats, and generates boarding passes.
 */
public class CheckInUI {

    private final Scanner scanner;
    private final CheckInManager checkInManager;

    /**
     * Constructs the check-in interface with the provided input scanner.
     *
     * @param scanner the input reader for capturing passenger check-in details
     */
    public CheckInUI(Scanner scanner) {
        this.scanner = scanner;
        this.checkInManager = CheckInManager.getInstance();
    }

    /**
     * Initiates the check-in sequence.
     * Validates the user's booking via PNR and coordinates the generation of boarding documents.
     */
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

            System.out.println("\n[1/3] Passengers Details & Documents Verified ✓");
            
            System.out.println("\n[2/3] Travel Preferences");
            System.out.print("Do any passengers require Special Assistance (Wheelchair, etc.)? (y/n): ");
            boolean specialAssistance = scanner.nextLine().trim().equalsIgnoreCase("y");

            System.out.println("\n[3/3] Seat Confirmation & Boarding Passes");
            
            java.util.List<BoardingPass> passes = new java.util.ArrayList<>();
            
            for (com.airline.skybooker.models.BookingPassenger bp : booking.getPassengers()) {
                System.out.println("\nPassenger: " + bp.getFullName());
                System.out.println("Currently assigned seat: " + bp.getSeatNumber());
                System.out.print("Would you like to keep this seat? (y/n): ");
                String keepSeat = scanner.nextLine().trim();

                if (keepSeat.equalsIgnoreCase("n")) {
                    System.out.println("Seat change requested. Note: Changing seats via Web UI is mocked here.");
                    System.out.print("Enter new seat (e.g. 14B): ");
                    bp.setSeatNumber(scanner.nextLine().trim());
                }

                String baggageInfo = bp.getBaggageWeight() > 0 ? bp.getBaggageWeight() + " kg" : "None";
                BoardingPass pass = checkInManager.createBoardingPass(booking, flight, bp, baggageInfo, specialAssistance);
                passes.add(pass);
            }
            
            checkInManager.finalizeCheckInState(booking);

            System.out.println("\n[SUCCESS] All " + passes.size() + " passengers are successfully checked in!");
            System.out.println("Your booking state is now: " + booking.getStatus());
            
            System.out.println("\nWould you like to print/view the Boarding Passes now? (y/n)");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                for (BoardingPass pass : passes) {
                    System.out.println(pass.getFormattedPass());
                }
            }

            System.out.println("Downloading Boarding Passes to local files...");
            for (BoardingPass pass : passes) {
                pass.downloadToFile();
            }

            System.out.println("Sending Boarding Passes via Email...");
            for (BoardingPass pass : passes) {
                NotificationManager.getInstance().sendBoardingPass(currentUser, pass);
            }

        } catch (BookingNotFoundException e) {
            System.out.println("[CHECK-IN FAILED] " + e.getMessage());
            ErrorLogger.logError(e);
        } catch (Exception e) {
            System.out.println("[CHECK-IN FAILED] An unexpected error occurred: " + e.getMessage());
            ErrorLogger.logError(e);
        }
    }
}
