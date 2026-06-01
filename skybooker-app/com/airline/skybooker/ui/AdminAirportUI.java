package com.airline.skybooker.ui;

import com.airline.skybooker.managers.AirportManager;
import com.airline.skybooker.models.Airport;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Admin UI for managing airports.
 */
public class AdminAirportUI {
    private final Scanner scanner;
    private final AirportManager airportManager;

    public AdminAirportUI(Scanner scanner) {
        this.scanner = scanner;
        this.airportManager = AirportManager.getInstance();
    }

    public void startAirportFlow() {
        while (true) {
            System.out.println("\n=== AIRPORT MANAGEMENT (ADMIN) ===");
            System.out.println("1. Add New Airport");
            System.out.println("2. Update Airport Details");
            System.out.println("3. Toggle Active/Inactive Status");
            System.out.println("4. List All Airports");
            System.out.println("5. Search & Retrieve Airports");
            System.out.println("0. Return to Dashboard");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                break;
            } else if (choice.equals("1")) {
                handleAddAirport();
            } else if (choice.equals("2")) {
                handleUpdateAirport();
            } else if (choice.equals("3")) {
                handleToggleStatus();
            } else if (choice.equals("4")) {
                handleListAll();
            } else if (choice.equals("5")) {
                handleSearchAirports();
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private void handleAddAirport() {
        System.out.println("\n--- ADD NEW AIRPORT ---");
        try {
            System.out.print("Enter Airport Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter IATA Code (e.g. LHR): ");
            String iata = scanner.nextLine().trim().toUpperCase();

            System.out.print("Enter City: ");
            String city = scanner.nextLine().trim();

            System.out.print("Enter Country: ");
            String country = scanner.nextLine().trim();

            System.out.print("Enter Timezone (e.g. Europe/London): ");
            String timezone = scanner.nextLine().trim();

            System.out.print("Enter Terminals (e.g. T1, T2): ");
            String terminals = scanner.nextLine().trim();

            System.out.print("Enter Facilities: ");
            String facilities = scanner.nextLine().trim();

            System.out.print("Enter Contact Email/Phone: ");
            String contactDetails = scanner.nextLine().trim();

            airportManager.createAirport(name, iata, city, country, timezone, terminals, facilities, contactDetails);
            System.out.println("[SUCCESS] Airport " + iata + " added to the global registry.");
        } catch (Exception e) {
            System.out.println("[FAILED] Error adding airport: " + e.getMessage());
        }
    }

    private void handleUpdateAirport() {
        System.out.print("Enter IATA Code of Airport to edit: ");
        String iata = scanner.nextLine().trim();
        Optional<Airport> opt = airportManager.getAirportByCode(iata);
        
        if (opt.isEmpty()) {
            System.out.println("[FAILED] Airport not found.");
            return;
        }

        System.out.print("Update Terminals (Press Enter to keep current): ");
        String term = scanner.nextLine().trim();

        System.out.print("Update Facilities (Press Enter to keep current): ");
        String fac = scanner.nextLine().trim();

        try {
            airportManager.updateAirport(iata, term, fac);
            System.out.println("[SUCCESS] Airport details updated.");
        } catch (IllegalArgumentException e) {
            System.out.println("[FAILED] " + e.getMessage());
        }
    }

    private void handleToggleStatus() {
        System.out.print("Enter IATA Code of Airport to toggle: ");
        String iata = scanner.nextLine().trim();
        Optional<Airport> opt = airportManager.getAirportByCode(iata);
        
        if (opt.isEmpty()) {
            System.out.println("[FAILED] Airport not found.");
            return;
        }

        try {
            boolean currentStatus = airportManager.toggleAirportStatus(iata);
            System.out.println("[SUCCESS] Airport " + iata.toUpperCase() + " is now " + (currentStatus ? "ACTIVE" : "INACTIVE"));
        } catch (IllegalArgumentException e) {
            System.out.println("[FAILED] " + e.getMessage());
        }
    }

    private void handleListAll() {
        System.out.println("\n--- GLOBAL AIRPORT REGISTRY ---");
        for (Airport a : airportManager.getAllAirports()) {
            System.out.println(a);
        }
    }

    private void handleSearchAirports() {
        System.out.println("\n--- ADVANCED AIRPORT SEARCH ---");
        System.out.print("Enter search query (Code, City, or Name): ");
        String query = scanner.nextLine().trim();

        List<Airport> results = airportManager.searchAirports(query);
        if (results.isEmpty()) {
            System.out.println("No airports found matching '" + query + "'.");
        } else {
            System.out.println("Found " + results.size() + " matches:");
            for (Airport a : results) {
                System.out.println(a.getFullDetails());
                System.out.println("-------------------------");
            }
        }
    }
}
