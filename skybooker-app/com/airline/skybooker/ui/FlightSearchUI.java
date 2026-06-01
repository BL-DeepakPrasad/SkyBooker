package com.airline.skybooker.ui;

import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.managers.AirportManager;
import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.models.Airport;
import com.airline.skybooker.filters.FlightFilterService;
import com.airline.skybooker.filters.PriceCriteria;
import com.airline.skybooker.filters.AirlineCriteria;
import com.airline.skybooker.exception.FlightNotFoundException;
import com.airline.skybooker.enums.TripType;
import com.airline.skybooker.utils.ValidationUtils;
import com.airline.skybooker.utils.InputReader;
import com.airline.skybooker.utils.ErrorLogger;

import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class FlightSearchUI {
    private final Scanner scanner;
    private final FlightManager flightManager;
    private final FlightFilterService filterService;
    private final BookingUI bookingUI;
    private final AuthenticationManager authManager;
    private final AirportManager airportManager;
    
    private int flightDisplayCounter = 1;
    private Map<Integer, Flight> flightDisplayMap = new HashMap<>();

    public FlightSearchUI(Scanner scanner, BookingUI bookingUI) {
        this.scanner = scanner;
        this.flightManager = FlightManager.getInstance();
        this.filterService = new FlightFilterService();
        this.authManager = AuthenticationManager.getInstance();
        this.airportManager = AirportManager.getInstance();
        this.bookingUI = bookingUI;
    }

    public void startSearchFlow() {
        flightDisplayCounter = 1;
        flightDisplayMap.clear();

        System.out.println("\n--- FLIGHT SEARCH ---");
        try {
            System.out.println("\nSelect Trip Type:");
            System.out.println("1. One-Way");
            System.out.println("2. Round-Trip");
            System.out.print("Enter choice (1/2): ");
            String tripChoice = scanner.nextLine().trim();
            TripType tripType = tripChoice.equals("2") ? TripType.ROUND_TRIP : TripType.ONE_WAY;

            System.out.println("\n[Auto-Suggest] Type a city or airport name to search (or press Enter if you know the code): ");
            String query = scanner.nextLine().trim();
            if (!query.isEmpty()) {
                List<Airport> suggestions = airportManager.searchAirports(query);
                if (suggestions.isEmpty()) {
                    System.out.println("No airports found matching '" + query + "'.");
                } else {
                    System.out.println("Suggested Airports:");
                    for (Airport a : suggestions) {
                        System.out.println("- " + a);
                    }
                }
            }

            String origin = getValidAirportCode("Enter Origin IATA Code (e.g. DEL): ");
            suggestAlternatives(origin);

            String destination = getValidAirportCode("Enter Destination IATA Code (e.g. BOM): ");
            suggestAlternatives(destination);

            System.out.print("\nAre your dates flexible? (y/n) to view ±3 Days Price Calendar: ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                showPriceCalendar(origin, destination);
            }

            System.out.println("\n--- OUTBOUND FLIGHTS (" + origin + " -> " + destination + ") ---");
            List<Flight> outboundFlights = flightManager.searchFlights(origin, destination);
            displayAndFilter(outboundFlights);

            if (tripType == TripType.ROUND_TRIP) {
                System.out.println("\n--- RETURN FLIGHTS (" + destination + " -> " + origin + ") ---");
                try {
                    List<Flight> returnFlights = flightManager.searchFlights(destination, origin);
                    displayAndFilter(returnFlights);
                } catch (FlightNotFoundException e) {
                    System.out.println("No return flights available for this route.");
                }
            }

            System.out.printf("\nAverage Fare for %s-%s: INR %.2f%n", origin, destination, flightManager.getAverageFare(origin, destination));
            flightManager.getCheapestFlight(origin, destination).ifPresent(f ->
                System.out.println("Cheapest Outbound Flight: INR " + f.getBasePrice())
            );

            System.out.print("\nEnter List Number (e.g. 1) or Flight Number (e.g. IG-202) to view details & book (or press Enter to skip): ");
            String fNumber = scanner.nextLine().trim();
            if (!fNumber.isEmpty()) {
                Flight selectedFlight = null;
                
                // Try to parse as List Number first
                try {
                    int selection = Integer.parseInt(fNumber);
                    selectedFlight = flightDisplayMap.get(selection);
                } catch (NumberFormatException e) {
                    // Fallback: search by Flight Number string
                    selectedFlight = flightManager.getFlightByNumber(fNumber).orElse(null);
                }
                
                if (selectedFlight != null) {
                    System.out.println(selectedFlight.getFullDetails());
                    
                    if (authManager.getCurrentUser().isPresent() && authManager.getCurrentUser().get() instanceof Passenger) {
                        Passenger currentPassenger = (Passenger) authManager.getCurrentUser().get();
                        bookingUI.startBookingFlow(currentPassenger, selectedFlight);
                    } else {
                        System.out.println("\n(You must be logged in as a Passenger to book this flight.)");
                    }
                } else {
                    System.out.println("Flight not found or invalid selection.");
                }
            }

        } catch (FlightNotFoundException e) {
            System.out.println("ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
            ErrorLogger.logError(e);
        }
    }

    private String getValidAirportCode(String prompt) {
        return InputReader.readString(scanner, "\n" + prompt, ValidationUtils::validateAirportCode).toUpperCase();
    }

    private void displayAndFilter(List<Flight> flights) {
        int pageSize = 3;
        int current = 0;
        
        while (current < flights.size()) {
            for (int i = current; i < Math.min(current + pageSize, flights.size()); i++) {
                Flight f = flights.get(i);
                System.out.println(flightDisplayCounter + ". " + f);
                flightDisplayMap.put(flightDisplayCounter, f);
                flightDisplayCounter++;
            }
            current += pageSize;
            if (current < flights.size()) {
                System.out.print("\n[Next Page (n) / Stop (Enter)]: ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("n")) {
                    break;
                }
            }
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
                    Flight f = filtered.get(i);
                    System.out.println(flightDisplayCounter + ". " + f);
                    flightDisplayMap.put(flightDisplayCounter, f);
                    flightDisplayCounter++;
                }
            }
        }
    }
    private void showPriceCalendar(String origin, String destination) {
        double avgFare = flightManager.getAverageFare(origin, destination);
        if (avgFare == 0.0) return;

        System.out.println("\n[Price Trend Calendar for " + origin + " -> " + destination + "]");
        System.out.printf("  -3 Days: INR %.2f%n", avgFare * 1.15);
        System.out.printf("  -2 Days: INR %.2f%n", avgFare * 1.05);
        System.out.printf("  -1 Day : INR %.2f%n", avgFare * 0.90);
        System.out.printf("   Target: INR %.2f  <-- Current Average%n", avgFare);
        System.out.printf("  +1 Day : INR %.2f%n", avgFare * 0.85);
        System.out.printf("  +2 Days: INR %.2f%n", avgFare * 0.95);
        System.out.printf("  +3 Days: INR %.2f%n", avgFare * 1.10);
    }

    private void suggestAlternatives(String iataCode) {
        List<Airport> alternatives = airportManager.getAlternativeAirports(iataCode);
        if (!alternatives.isEmpty()) {
            System.out.println("  [Tip] Alternative nearby airports in the same city: ");
            for (Airport a : alternatives) {
                System.out.println("   -> " + a.getIataCode() + " (" + a.getName() + ")");
            }
        }
    }
}
