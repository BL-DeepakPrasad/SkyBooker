package com.airline.skybooker.ui;

import com.airline.skybooker.enums.FlightStatus;
import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.models.Airline;
import com.airline.skybooker.models.Airport;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.services.SeatService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Scanner;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UI Controller for Module 8 (Admin Flight Management).
 * Handles the creation, modification, and reporting of flights.
 */
public class AdminFlightUI {
    private final Scanner scanner;
    private final FlightManager flightManager;
    private final SeatService seatService;

    public AdminFlightUI(Scanner scanner, SeatService seatService) {
        this.scanner = scanner;
        this.flightManager = FlightManager.getInstance();
        this.seatService = seatService;
    }

    public void startAdminFlow() {
        while (true) {
            System.out.println("\n=== FLIGHT MANAGEMENT (ADMIN/STAFF) ===");
            System.out.println("1. Create New Flight");
            System.out.println("2. View All Flights");
            System.out.println("3. Edit Existing Flight");
            System.out.println("4. Generate Occupancy Reports");
            System.out.println("0. Return to Dashboard");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                break;
            } else if (choice.equals("1")) {
                handleFlightCreation();
            } else if (choice.equals("2")) {
                handleViewAllFlights();
            } else if (choice.equals("3")) {
                handleFlightEdit();
            } else if (choice.equals("4")) {
                handleGenerateReport();
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private void handleFlightCreation() {
        System.out.println("\n--- FLIGHT CREATION WIZARD ---");
        try {
            System.out.print("Enter Airline Name (e.g., Air India): ");
            String airlineName = scanner.nextLine().trim();
            System.out.print("Enter Airline Code (e.g., AI): ");
            String airlineCode = scanner.nextLine().trim();

            System.out.print("Enter Flight Number (e.g., AI-999): ");
            String flightNumber = scanner.nextLine().trim().toUpperCase();

            System.out.print("Enter Aircraft Type (e.g., Boeing 777): ");
            String aircraftType = scanner.nextLine().trim();

            System.out.print("Enter Origin Airport Code (e.g. DEL): ");
            String originCode = scanner.nextLine().trim().toUpperCase();

            System.out.print("Enter Destination Airport Code (e.g. BOM): ");
            String destCode = scanner.nextLine().trim().toUpperCase();

            System.out.print("Enter Seat Capacity (e.g., 60): ");
            int capacity = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Base Fare (e.g., 500.00): ");
            double baseFare = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Enter Baggage Policy: ");
            String baggage = scanner.nextLine().trim();

            System.out.print("Enter Cancellation Policy: ");
            String cancelPolicy = scanner.nextLine().trim();

            System.out.print("Enter Amenities (e.g., Wi-Fi, Meals): ");
            String amenities = scanner.nextLine().trim();

            flightManager.createFlight(airlineName, airlineCode, flightNumber, aircraftType, originCode, destCode, capacity, baseFare, baggage, cancelPolicy, amenities, seatService);

            System.out.println("[SUCCESS] Flight " + flightNumber + " created and published successfully!");

        } catch (Exception e) {
            System.out.println("[FAILED] Error creating flight: " + e.getMessage());
        }
    }

    private void handleViewAllFlights() {
        System.out.println("\n--- ALL PUBLISHED FLIGHTS ---");
        for (Flight f : flightManager.getAllFlights()) {
            System.out.println(f);
        }
    }

    private void handleFlightEdit() {
        System.out.println("\n--- FLIGHT INFORMATION MANAGEMENT ---");
        System.out.print("Enter Flight Number to Edit (e.g. AI-101): ");
        String flightNum = scanner.nextLine().trim();

        Optional<Flight> flightOpt = flightManager.getFlightByNumber(flightNum);
        if (flightOpt.isEmpty()) {
            System.out.println("[FAILED] Flight not found.");
            return;
        }
        System.out.println("Editing Flight: " + flightNum);
        System.out.println("1. Update Departure Time");
        System.out.println("2. Change Base Fare");
        System.out.println("3. Apply Dynamic Pricing (Demand Surge/Drop)");
        System.out.println("4. Update Flight Status (Delay/Cancel)");
        System.out.print("Enter choice: ");
        
        String choice = scanner.nextLine().trim();
        try {
            switch (choice) {
                case "1":
                    System.out.print("Enter extra days to delay departure: ");
                    int days = Integer.parseInt(scanner.nextLine().trim());
                    flightManager.updateFlightDeparture(flightNum, days);
                    System.out.println("[SUCCESS] Departure time updated.");
                    break;
                case "2":
                    System.out.print("Enter new base fare: ");
                    double fare = Double.parseDouble(scanner.nextLine().trim());
                    flightManager.updateFlightFare(flightNum, fare);
                    System.out.println("[SUCCESS] Base fare updated to $" + fare);
                    break;
                case "3":
                    System.out.print("Enter percentage change (e.g. 20 for +20%, -10 for -10%): ");
                    double pct = Double.parseDouble(scanner.nextLine().trim());
                    flightManager.applyDynamicPricing(flightNum, pct);
                    System.out.println("[SUCCESS] Dynamic pricing applied.");
                    break;
                case "4":
                    System.out.println("1. SCHEDULED");
                    System.out.println("2. DELAYED");
                    System.out.println("3. CANCELLED");
                    System.out.print("Select Status: ");
                    String sChoice = scanner.nextLine().trim();
                    if (sChoice.equals("2")) {
                        flightManager.updateFlightStatus(flightNum, FlightStatus.DELAYED);
                        System.out.println("[SUCCESS] Status changed to DELAYED.");
                        System.out.println("\n[BROADCAST] Notification published to all booked passengers.");
                    } else if (sChoice.equals("3")) {
                        flightManager.updateFlightStatus(flightNum, FlightStatus.CANCELLED);
                        System.out.println("[SUCCESS] Status changed to CANCELLED.");
                    } else {
                        flightManager.updateFlightStatus(flightNum, FlightStatus.SCHEDULED);
                        System.out.println("[SUCCESS] Status changed to SCHEDULED.");
                    }
                    break;
                default:
                    System.out.println("Invalid option.");
                    return; 
            }
        } catch (Exception e) {
            System.out.println("[FAILED] Error updating flight: " + e.getMessage());
        }
    }

    private void handleGenerateReport() {
        System.out.println("\n--- FLIGHT SEARCH & OCCUPANCY REPORT ---");
        System.out.println("Apply filters (press Enter to skip any filter):");
        
        System.out.print("Filter by Airline Code (e.g. AI): ");
        String airlineCode = scanner.nextLine().trim().toUpperCase();

        System.out.print("Filter by Route (e.g. DEL-BOM): ");
        String route = scanner.nextLine().trim().toUpperCase();

        System.out.print("Filter by Status (SCHEDULED, DELAYED, CANCELLED): ");
        String statusStr = scanner.nextLine().trim().toUpperCase();

        List<Flight> filteredFlights = flightManager.getFilteredFlights(airlineCode, route, statusStr);

        System.out.println("\n--- REPORT RESULTS ---");
        System.out.printf("%-10s | %-10s | %-12s | %-10s | %-15s%n", "FLIGHT", "ROUTE", "STATUS", "CAPACITY", "OCCUPANCY (%)");
        System.out.println("-----------------------------------------------------------------------");
        
        if (filteredFlights.isEmpty()) {
            System.out.println("No flights matched the reporting criteria.");
        } else {
            for (Flight f : filteredFlights) {
                String fRoute = f.getOrigin().getIataCode() + "-" + f.getDestination().getIataCode();
                System.out.printf("%-10s | %-10s | %-12s | %d seats   | %.1f%%%n", 
                    f.getFlightNumber(), 
                    fRoute, 
                    f.getFlightStatus().name(), 
                    f.getTotalCapacity(),
                    f.getOccupancyRate()
                );
            }
        }
    }
}
