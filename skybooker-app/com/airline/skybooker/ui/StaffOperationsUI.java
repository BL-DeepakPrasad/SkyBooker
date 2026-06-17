package com.airline.skybooker.ui;

import com.airline.skybooker.managers.BookingManager;
import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.BookingPassenger;
import com.airline.skybooker.models.Flight;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Provides a menu specifically for airline staff to manage day-to-day flight operations.
 * Staff can view the list of passengers on a flight or assign pilots and cabin crew.
 */
public class StaffOperationsUI {
    private final Scanner scanner;
    private final FlightManager flightManager;
    private final BookingManager bookingManager;

    /**
     * Sets up the staff operations menu using a Scanner for reading user input.
     *
     * @param scanner reads text typed by the staff member in the console
     */
    public StaffOperationsUI(Scanner scanner) {
        this.scanner = scanner;
        this.flightManager = FlightManager.getInstance();
        this.bookingManager = BookingManager.getInstance();
    }

    /**
     * Asks for a flight number and prints out a list of every passenger booked on that flight.
     * It automatically hides anyone who has cancelled their ticket.
     */
    public void handleViewManifest() {
        System.out.println("\n--- PASSENGER MANIFEST ---");
        System.out.print("Enter Flight Number (e.g. AI-101): ");
        String flightNum = scanner.nextLine().trim();

        Optional<Flight> flightOpt = flightManager.getFlightByNumber(flightNum);
        if (flightOpt.isEmpty()) {
            System.out.println("[FAILED] Flight not found.");
            return;
        }
        
        Flight flight = flightOpt.get();
        System.out.println("Manifest for Flight: " + flight.getFlightNumber() + " (" + flight.getOrigin().getIataCode() + " -> " + flight.getDestination().getIataCode() + ")");
        System.out.println("------------------------------------------------------------");

        List<Booking> bookings = bookingManager.getAllBookings().stream()
                .filter(b -> b.getFlightId() == flight.getFlightId())
                .filter(b -> !b.getStatus().equals("CANCELLED") && !b.getStatus().equals("REFUNDED"))
                .toList();

        if (bookings.isEmpty()) {
            System.out.println("No passengers are booked on this flight.");
            return;
        }

        int count = 1;
        System.out.printf("%-5s | %-20s | %-15s | %-10s%n", "No.", "Passenger Name", "Passport", "Seat");
        System.out.println("------------------------------------------------------------");
        for (Booking booking : bookings) {
            for (BookingPassenger bp : booking.getPassengers()) {
                if (!bp.isCancelled()) {
                    System.out.printf("%-5d | %-20s | %-15s | %-10s%n", count++, bp.getFullName(), bp.getPassportNumber(), bp.getSeatNumber());
                }
            }
        }
        System.out.println("------------------------------------------------------------");
        System.out.println("Total Active Passengers: " + (count - 1));
    }

    /**
     * Lets staff view who is working on a specific flight, and add or remove crew members like pilots and flight attendants.
     */
    public void handleCrewSchedules() {
        System.out.println("\n--- MANAGE CREW SCHEDULES ---");
        System.out.print("Enter Flight Number (e.g. AI-101): ");
        String flightNum = scanner.nextLine().trim();

        Optional<Flight> flightOpt = flightManager.getFlightByNumber(flightNum);
        if (flightOpt.isEmpty()) {
            System.out.println("[FAILED] Flight not found.");
            return;
        }
        
        Flight flight = flightOpt.get();

        while (true) {
            System.out.println("\nCrew assigned to " + flight.getFlightNumber() + ":");
            List<String> crew = flight.getAssignedCrew();
            if (crew.isEmpty()) {
                System.out.println("  (No crew assigned yet)");
            } else {
                for (int i = 0; i < crew.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + crew.get(i));
                }
            }

            System.out.println("\n1. Add Crew Member");
            System.out.println("2. Remove Crew Member");
            System.out.println("0. Go Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                break;
            } else if (choice.equals("1")) {
                System.out.print("Enter Crew Member Name and Role (e.g. 'Capt. John Doe - Pilot'): ");
                String name = scanner.nextLine().trim();
                flight.addCrewMember(name);
                System.out.println("[SUCCESS] Crew member added.");
            } else if (choice.equals("2")) {
                System.out.print("Enter the number of the crew member to remove: ");
                try {
                    int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (idx >= 0 && idx < crew.size()) {
                        flight.removeCrewMember(crew.get(idx));
                        System.out.println("[SUCCESS] Crew member removed.");
                    } else {
                        System.out.println("Invalid selection.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                }
            } else {
                System.out.println("Invalid option.");
            }
        }
    }
}
