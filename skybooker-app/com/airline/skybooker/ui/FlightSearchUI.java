package com.airline.skybooker.ui;

import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.filters.FlightFilterService;
import com.airline.skybooker.filters.PriceCriteria;
import com.airline.skybooker.filters.AirlineCriteria;
import com.airline.skybooker.exception.FlightNotFoundException;

import java.util.List;
import java.util.Scanner;

public class FlightSearchUI {
    private final Scanner scanner;
    private final FlightManager flightManager;
    private final FlightFilterService filterService;
    private final BookingUI bookingUI;
    private final AuthenticationManager authManager;

    public FlightSearchUI(Scanner scanner, BookingUI bookingUI) {
        this.scanner = scanner;
        this.flightManager = FlightManager.getInstance();
        this.filterService = new FlightFilterService();
        this.authManager = AuthenticationManager.getInstance();
        this.bookingUI = bookingUI;
    }

    public void startSearchFlow() {
        System.out.println("\n--- FLIGHT SEARCH ---");
        try {
            System.out.println("\nSelect Trip Type:");
            System.out.println("1. One-Way");
            System.out.println("2. Round-Trip");
            System.out.print("Enter choice (1/2): ");
            boolean isRoundTrip = scanner.nextLine().trim().equals("2");

            System.out.print("Enter Origin IATA Code (e.g. DEL): ");
            String origin = scanner.nextLine().trim().toUpperCase();

            System.out.print("Enter Destination IATA Code (e.g. BOM): ");
            String destination = scanner.nextLine().trim().toUpperCase();

            System.out.println("\n--- OUTBOUND FLIGHTS (" + origin + " -> " + destination + ") ---");
            List<Flight> outboundFlights = flightManager.searchFlights(origin, destination);
            displayAndFilter(outboundFlights);

            if (isRoundTrip) {
                System.out.println("\n--- RETURN FLIGHTS (" + destination + " -> " + origin + ") ---");
                try {
                    List<Flight> returnFlights = flightManager.searchFlights(destination, origin);
                    displayAndFilter(returnFlights);
                } catch (FlightNotFoundException e) {
                    System.out.println("No return flights available for this route.");
                }
            }

            System.out.printf("\nAverage Fare for %s-%s: $%.2f%n", origin, destination, flightManager.getAverageFare(origin, destination));
            flightManager.getCheapestFlight(origin, destination).ifPresent(f ->
                System.out.println("Cheapest Outbound Flight: $" + f.getBasePrice())
            );

            System.out.print("\nEnter Flight Number to view full details (or press Enter to skip): ");
            String fNumber = scanner.nextLine().trim();
            if (!fNumber.isEmpty()) {
                flightManager.getFlightByNumber(fNumber).ifPresentOrElse(
                    flight -> {
                        System.out.println(flight.getFullDetails());
                        
                        if (authManager.getCurrentUser().isPresent() && authManager.getCurrentUser().get() instanceof Passenger) {
                            Passenger currentPassenger = (Passenger) authManager.getCurrentUser().get();
                            bookingUI.startBookingFlow(currentPassenger, flight);
                        } else {
                            System.out.println("\n(You must be logged in as a Passenger to book this flight.)");
                        }
                    },
                    () -> System.out.println("Flight not found.")
                );
            }

        } catch (FlightNotFoundException e) {
            System.out.println("ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void displayAndFilter(List<Flight> flights) {
        for (int i = 0; i < flights.size(); i++) {
            System.out.println((i + 1) + ". " + flights.get(i));
        }

        System.out.print("\nDo you want to apply filters? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.print("Enter Max Price (or press Enter to skip): ");
            String priceInput = scanner.nextLine().trim();
            Double maxPrice = priceInput.isEmpty() ? null : Double.parseDouble(priceInput);

            System.out.print("Enter Airline ID [100=Air India, 101=IndiGo] (or press Enter to skip): ");
            String airlineInput = scanner.nextLine().trim();
            Integer airlineId = airlineInput.isEmpty() ? null : Integer.parseInt(airlineInput);

            List<Flight> filtered = filterService.filter(flights, 
                new PriceCriteria(maxPrice),
                new AirlineCriteria(airlineId)
            );
            
            System.out.println("\n--- FILTERED RESULTS ---");
            if (filtered.isEmpty()) {
                System.out.println("No flights match your filters.");
            } else {
                for (int i = 0; i < filtered.size(); i++) {
                    System.out.println((i + 1) + ". " + filtered.get(i));
                }
            }
        }
    }
}
