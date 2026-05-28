package com.airline.skybooker;

import com.airline.skybooker.exception.FlightNotFoundException;
import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.models.Flight;

import java.util.List;
import java.util.Scanner;

/**
 * Main application entry point for the Flight Search module.
 * This class provides a command-line interface for users to search the inventory
 * for available outbound flights.
 */
public class Main {

    /**
     * Executes the flight search interactive flow.
     * Prompts the user for Origin and Destination data, invokes the search services,
     * and delegates rendering to the standard output.
     *
     * @param args command-line arguments (not utilized)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FlightManager flightManager = FlightManager.getInstance();

        System.out.println("=== SKYBOOKER FLIGHT SEARCH ===");

        try {
            System.out.println("\nSelect Trip Type:");
            System.out.println("1. One-Way");
            System.out.println("2. Round-Trip");
            System.out.print("Enter choice (1/2): ");
            String tripChoice = scanner.nextLine();
            boolean isRoundTrip = tripChoice.equals("2");

            System.out.print("Enter Origin IATA Code (e.g. DEL): ");
            String origin = scanner.nextLine().toUpperCase();

            System.out.print("Enter Destination IATA Code (e.g. BOM): ");
            String destination = scanner.nextLine().toUpperCase();

            System.out.println("\n--- OUTBOUND FLIGHTS (" + origin + " -> " + destination + ") ---");
            List<Flight> outboundFlights = flightManager.searchFlights(origin, destination);
            for (int i = 0; i < outboundFlights.size(); i++) {
                System.out.println((i + 1) + ". " + outboundFlights.get(i));
            }
            
            if (isRoundTrip) {
                System.out.println("\n--- RETURN FLIGHTS (" + destination + " -> " + origin + ") ---");
                try {
                    List<Flight> returnFlights = flightManager.searchFlights(destination, origin);
                    for (int i = 0; i < returnFlights.size(); i++) {
                        System.out.println((i + 1) + ". " + returnFlights.get(i));
                    }
                } catch (FlightNotFoundException e) {
                    System.out.println("No return flights available for this route.");
                }
            }

            System.out.printf("\nAverage Fare for %s-%s: $%.2f%n", origin, destination, flightManager.getAverageFare(origin, destination));
            flightManager.getCheapestFlight(origin, destination).ifPresent(f ->
                System.out.println("Cheapest Outbound Flight available: $" + f.getBasePrice())
            );

        } catch (FlightNotFoundException e) {
            System.out.println("ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
