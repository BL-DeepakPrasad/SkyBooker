package com.airline.skybooker;

import com.airline.skybooker.exception.FlightNotFoundException;
import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.exception.SeatLockException;
import com.airline.skybooker.filters.FlightFilterService;
import com.airline.skybooker.filters.PriceCriteria;
import com.airline.skybooker.filters.AirlineCriteria;

import java.util.List;
import java.util.Scanner;

/**
 * Main application entry point for the Flight Search module.
 */
public class Main {
    private final Scanner scanner;
    private final FlightManager flightManager;
    private final FlightFilterService filterService;
    private final SeatService seatService;
    private final AuthenticationManager authManager;

    public Main() {
        this.scanner = new Scanner(System.in);
        this.flightManager = FlightManager.getInstance();
        this.filterService = new FlightFilterService();
        this.seatService = new SeatService();
        this.authManager = AuthenticationManager.getInstance();
    }

    /**
     * Bootstraps the application.
     */
    public void start() {
        System.out.println("=== WELCOME TO SKYBOOKER ===");
        
        while (authManager.getCurrentUser().isEmpty()) {
            System.out.println("\n1. Login");
            System.out.println("2. Register as Passenger");
            System.out.println("3. Continue as Guest");
            System.out.print("Enter choice (1-3): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                handleLogin();
            } else if (choice.equals("2")) {
                handleRegistration();
            } else if (choice.equals("3")) {
                break; // Continue without login
            }
        }

        // Polymorphic Dashboard Display & Interaction
        if (authManager.getCurrentUser().isPresent()) {
            User user = authManager.getCurrentUser().get();
            boolean inDashboard = true;
            while (inDashboard) {
                user.displayDashboard();
                System.out.println("4. Continue to Flight Search");
                System.out.print("Enter choice: ");
                String dashChoice = scanner.nextLine().trim();

                if (dashChoice.equals("1") && user instanceof Passenger) {
                    handleViewProfile((Passenger) user);
                } else if (dashChoice.equals("3") && user instanceof Passenger) {
                    handleProfileUpdate((Passenger) user);
                } else if (dashChoice.equals("4")) {
                    inDashboard = false;
                } else {
                    System.out.println("Feature coming soon!");
                }
            }
        }

        // Flight Search Flow
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
                        
                        // Use Case 5: Select Seats
                        System.out.print("\nDo you want to select a seat for this flight? (y/n): ");
                        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                            seatService.displaySeatMap(flight.getFlightNumber());
                            System.out.print("\nEnter Seat Number to lock (e.g. 1B): ");
                            String seatNum = scanner.nextLine().trim();
                            try {
                                if (seatService.lockSeat(flight.getFlightNumber(), seatNum)) {
                                    System.out.println(" SUCCESS: Seat " + seatNum + " has been locked for you for 10 minutes.");
                                }
                            } catch (SeatLockException ex) {
                                System.out.println(" FAILED: " + ex.getMessage());
                            }
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

    private void handleLogin() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();
        
        if (authManager.login(email, pass)) {
            System.out.println("Login Successful!");
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    private void handleRegistration() {
        try {
            System.out.print("Full Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password: ");
            String pass = scanner.nextLine().trim();
            System.out.print("Phone: ");
            String phone = scanner.nextLine().trim();
            System.out.print("Passport Number: ");
            String passport = scanner.nextLine().trim();
            System.out.print("Nationality: ");
            String nationality = scanner.nextLine().trim();

            authManager.registerPassenger(name, email, pass, phone, passport, nationality);
            System.out.println("Registration Successful! Please login.");
        } catch (Exception e) {
            System.out.println("Registration Failed: " + e.getMessage());
        }
    }

    private void handleViewProfile(Passenger passenger) {
        System.out.println("\n============================================");
        System.out.println("               USER PROFILE                 ");
        System.out.println("============================================");
        System.out.println("Name:        " + passenger.getFullName());
        System.out.println("Email:       " + passenger.getEmail());
        System.out.println("Phone:       " + passenger.getPhone());
        System.out.println("Nationality: " + passenger.getNationality());
        System.out.println("Passport:    " + passenger.getPassportNumber());
        System.out.println("Role:        " + passenger.getRole());
        System.out.println("============================================");
        System.out.println("Press Enter to return to Dashboard...");
        scanner.nextLine();
    }

    private void handleProfileUpdate(Passenger passenger) {
        System.out.println("\n--- UPDATE PROFILE ---");
        System.out.println("Leave blank to keep current value.");
        
        System.out.print("New Phone Number [" + passenger.getPhone() + "]: ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty()) passenger.setPhone(phone);

        System.out.print("New Passport Number [" + passenger.getPassportNumber() + "]: ");
        String passport = scanner.nextLine().trim();
        if (!passport.isEmpty()) passenger.setPassportNumber(passport);

        System.out.print("New Nationality [" + passenger.getNationality() + "]: ");
        String nationality = scanner.nextLine().trim();
        if (!nationality.isEmpty()) passenger.setNationality(nationality);

        System.out.println(" Profile updated successfully!");
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

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
}