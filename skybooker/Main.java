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
            System.out.print("Enter Origin IATA Code (e.g. DEL): ");
            String origin = scanner.nextLine().toUpperCase();

            System.out.print("Enter Destination IATA Code (e.g. BOM): ");
            String destination = scanner.nextLine().toUpperCase();

            List<Flight> flights = flightManager.searchFlights(origin, destination);

            System.out.println("\n--- Available Flights ---");
            for (int i = 0; i < flights.size(); i++) {
                System.out.println((i + 1) + ". " + flights.get(i));
            }

            System.out.printf("\nAverage Fare for %s-%s: $%.2f%n", origin, destination, flightManager.getAverageFare(origin, destination));
            flightManager.getCheapestFlight(origin, destination).ifPresent(f ->
                System.out.println("Cheapest Flight available: $" + f.getBasePrice())
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
