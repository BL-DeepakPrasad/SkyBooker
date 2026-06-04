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

/**
 * Helps users search for flights to their destination.
 * It provides a list of available flights, lets users filter by price or airline, 
 * and shows tips like alternative nearby airports or cheaper dates to fly.
 */
public class FlightSearchUI {
    private final Scanner scanner;
    private final FlightManager flightManager;
    private final FlightFilterService filterService;
    private final BookingUI bookingUI;
    private final AuthenticationManager authManager;
    private final AirportManager airportManager;
    
    private int flightDisplayCounter = 1;
    private Map<Integer, Flight> flightDisplayMap = new HashMap<>();

    /**
     * Sets up the flight search screen.
     *
     * @param scanner reads text typed by the user in the console
     * @param bookingUI the screen we jump to if the user decides to book a flight they found
     */
    public FlightSearchUI(Scanner scanner, BookingUI bookingUI) {
        this.scanner = scanner;
        this.flightManager = FlightManager.getInstance();
        this.filterService = new FlightFilterService();
        this.authManager = AuthenticationManager.getInstance();
        this.airportManager = AirportManager.getInstance();
        this.bookingUI = bookingUI;
    }

    /**
     * Asks the user where they want to go and searches the system for matching flights.
     * It then shows the results so the user can choose one to book.
     */
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

    /**
     * Asks the user to type in a 3-letter airport code (like JFK or DEL) and checks if it's valid.
     *
     * @param prompt the text asking the user for input
     * @return the correctly formatted airport code
     */
    private String getValidAirportCode(String prompt) {
        return InputReader.readString(scanner, "\n" + prompt, ValidationUtils::validateAirportCode).toUpperCase();
    }

    /**
     * Shows a list of flights a few at a time so the screen doesn't get cluttered.
     * It also asks if the user wants to hide expensive flights or filter by a specific airline.
     *
     * @param flights the list of flights we found for the user's route
     */
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
    /**
     * Shows estimated prices for the days before and after the chosen date.
     * This helps users save money if their travel dates are flexible.
     */
    private void showPriceCalendar(String origin, String destination) {
        java.util.Optional<Flight> cheapestOpt = flightManager.getCheapestFlight(origin, destination);
        if (!cheapestOpt.isPresent()) return;
        
        Flight cheapestFlight = cheapestOpt.get();
        double cheapestFare = cheapestFlight.getBasePrice();
        java.time.LocalDate baseDate = cheapestFlight.getDepartureTime().toLocalDate();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd");

        System.out.println("\n[Price Trend Calendar for " + origin + " -> " + destination + "]");
        System.out.printf("  %s (-3 Days): INR %.2f%n", baseDate.minusDays(3).format(formatter), cheapestFare * 1.15);
        System.out.printf("  %s (-2 Days): INR %.2f%n", baseDate.minusDays(2).format(formatter), cheapestFare * 1.05);
        System.out.printf("  %s (-1 Day ): INR %.2f%n", baseDate.minusDays(1).format(formatter), cheapestFare * 0.90);
        System.out.printf("  %s (Target) : INR %.2f  <-- Current Cheapest%n", baseDate.format(formatter), cheapestFare);
        System.out.printf("  %s (+1 Day ): INR %.2f%n", baseDate.plusDays(1).format(formatter), cheapestFare * 0.85);
        System.out.printf("  %s (+2 Days): INR %.2f%n", baseDate.plusDays(2).format(formatter), cheapestFare * 0.95);
        System.out.printf("  %s (+3 Days): INR %.2f%n", baseDate.plusDays(3).format(formatter), cheapestFare * 1.10);
    }

    /**
     * Suggests other airports in the same city.
     * For example, if a user searches for Heathrow (LHR), it might suggest Gatwick (LGW) as an option.
     */
    private void suggestAlternatives(String iataCode) {
        List<Airport> alternatives = airportManager.getAlternativeAirports(iataCode);
        if (!alternatives.isEmpty()) {
            System.out.println(" [Tip] Alternative nearby airports in the same city: ");
            for (Airport a : alternatives) {
                System.out.println("   -> " + a.getIataCode() + " (" + a.getName() + ")");
            }
        }
    }
}
