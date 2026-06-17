package com.airline.skybooker.ui;

import com.airline.skybooker.managers.AirportManager;
import com.airline.skybooker.models.Airport;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Provides a text-based menu for admins to manage airports in the system.
 * This class handles all user inputs for airport-related tasks like adding new airports,
 * updating their details, or marking them as inactive if they shut down.
 */
public class AdminAirportUI {
    private final Scanner scanner;
    private final AirportManager airportManager;

    /**
     * Sets up the admin airport menu using a Scanner for reading user input.
     *
     * @param scanner reads text typed by the admin in the console
     */
    public AdminAirportUI(Scanner scanner) {
        this.scanner = scanner;
        this.airportManager = AirportManager.getInstance();
    }

    /**
     * Shows the main airport management menu and keeps it running in a loop.
     * Based on what the admin types, it calls the correct method to handle the chosen task.
     */
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

    /**
     * Asks the admin step-by-step for the details needed to create a new airport.
     * Once all details are gathered, it saves the new airport to the system.
     */
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

    /**
     * Lets the admin change specific details of an existing airport, like adding new terminals.
     * It asks for the airport's IATA code first to ensure we update the correct one.
     */
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

    /**
     * Marks an airport as either active or inactive. 
     * This is useful if an airport temporarily closes or reopens, preventing flights from being scheduled there.
     */
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

    /**
     * Prints out a list of every airport currently stored in the system.
     */
    private void handleListAll() {
        System.out.println("\n--- GLOBAL AIRPORT REGISTRY ---");
        for (Airport a : airportManager.getAllAirports()) {
            System.out.println(a);
        }
    }

    /**
     * Searches for airports matching a specific keyword (like "Delhi" or "DEL").
     * This helps admins quickly find airports without scrolling through the entire list.
     */
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
