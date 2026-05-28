package com.airline.skybooker;

import com.airline.skybooker.exception.FlightNotFoundException;
import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.models.Flight;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FlightManager flightManager = FlightManager.getInstance();

        System.out.println("=== SKYBOOKER FLIGHT SEARCH (UC1) ===");

        try {
            System.out.print("Enter Origin IATA Code (e.g. DEL): ");
            String origin = scanner.nextLine().toUpperCase();

            System.out.print("Enter Destination IATA Code (e.g. BOM): ");
            String destination = scanner.nextLine().toUpperCase();

            // UC1: Searching Flights
            List<Flight> flights = flightManager.searchFlights(origin, destination);

            System.out.println("\n--- Available Flights ---");
            for (int i = 0; i < flights.size(); i++) {
                System.out.println((i + 1) + ". " + flights.get(i));
            }

            // Demonstrating Streams Aggregations
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
