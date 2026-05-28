package com.airline.skybooker;

import com.airline.skybooker.enums.TripType;
import com.airline.skybooker.exception.*;
import com.airline.skybooker.managers.BookingManager;
import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.models.*;
import com.airline.skybooker.security.AuthenticationManager;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;


public class App {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static final AuthenticationManager authManager =
            AuthenticationManager.getInstance();

    private static final FlightManager flightManager =
            FlightManager.getInstance();

    private static final BookingManager bookingManager =
            BookingManager.getInstance();

    private static User loginFlow() {

        System.out.println("--- SYSTEM LOGIN ---");

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (!authManager.authenticate(email, password)) {
            System.out.println("Invalid credentials.");
            return null;
        }

        String actualOtp = authManager.generateOTP(email);

        System.out.print("Enter OTP: ");
        String inputOtp = scanner.nextLine();

        User user = authManager.verifyOTPAndLogin(email, inputOtp, actualOtp);
        if (user == null) {
            System.out.println("OTP verification failed.");
            return null;
        }

        user.displayDashboard();

        return user;
    }

    private static void showMainMenu(User activeUser) {

        boolean running = true;

        while (running) {

            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Search & Book Flights (One-Way)");
            System.out.println("2. Search & Book Flights (Round-Trip)");
            System.out.println("3. View My Bookings");
            System.out.println("4. Process Queue");
            System.out.println("5. Logout");

            System.out.print("Enter choice: ");

            int choice;

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {

                case 1:
                    searchAndBookFlight(activeUser, false);
                    break;
                    
                case 2:
                    searchAndBookFlight(activeUser, true);
                    break;

                case 3:
                    viewUserBookings(activeUser);
                    break;

                case 4:
                    processBookingQueue(activeUser);
                    break;

                case 5:
                    logout();
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewUserBookings(User activeUser) {
        System.out.println("\n--- My Bookings ---");
        List<Booking> userBookings = bookingManager.getBookingsByUser(activeUser.getUserId());
        
        if (userBookings.isEmpty()) {
            System.out.println("You have no bookings.");
        } else {
            for (Booking b : userBookings) {
                System.out.println(b);
            }
        }
    }

    private static void searchAndBookFlight(User activeUser, boolean isRoundTrip) {

        try {

            System.out.print("Enter Origin: ");
            String origin = scanner.nextLine().toUpperCase();

            System.out.print("Enter Destination: ");
            String destination = scanner.nextLine().toUpperCase();

            System.out.println("\n--- OUTBOUND FLIGHTS ---");
            List<Flight> outboundFlights = flightManager.searchFlights(origin, destination);
            displayFlights(outboundFlights);
            Flight selectedOutbound = selectFlight(outboundFlights);

            if (selectedOutbound == null) return;
            
            Flight selectedReturn = null;
            if (isRoundTrip) {
                System.out.println("\n--- RETURN FLIGHTS ---");
                List<Flight> returnFlights = flightManager.searchFlights(destination, origin);
                displayFlights(returnFlights);
                selectedReturn = selectFlight(returnFlights);
                
                if (selectedReturn == null) return;
            }

            Passenger passenger = createPassenger(activeUser);

            System.out.print("Enter Seat Number for Outbound: ");
            String seatOut = scanner.nextLine().toUpperCase();
            bookingManager.allocateSeat(selectedOutbound, passenger, seatOut);
            
            if (isRoundTrip && selectedReturn != null) {
                System.out.print("Enter Seat Number for Return: ");
                String seatRet = scanner.nextLine().toUpperCase();
                bookingManager.allocateSeat(selectedReturn, passenger, seatRet);
            }

            TripType tripType = chooseTripType();

            Booking booking = bookingManager.createBooking(
                    activeUser,
                    selectedOutbound,
                    selectedReturn,
                    Arrays.asList(passenger),
                    tripType
            );

            System.out.println("Booking Created!");
            System.out.println(booking);

            System.out.printf("Average Fare for %s-%s: $%.2f%n", origin, destination, flightManager.getAverageFare(origin, destination));
            flightManager.getCheapestFlight(origin, destination).ifPresent(f -> 
                System.out.println("Cheapest Outbound Flight available: $" + f.getBasePrice())
            );

            System.out.print("Enter Credit Card Number to pay $" + booking.getTotalFare() + ": ");
            String card = scanner.nextLine();
            CardPayment payment = new CardPayment(booking.getTotalFare(), card);
            
            if(payment.processPayment(booking.getTotalFare())) {
                System.out.println("Payment Successful. Booking is queued for Admin confirmation.");
                System.out.println("Current status: " + booking.getState().getStatusString());
            }

        } catch (FlightNotFoundException | SeatUnavailableException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred during booking", e);
        }
    }

    private static void displayFlights(List<Flight> flights) {

        System.out.println("\n--- Available Flights ---");

        for (int i = 0; i < flights.size(); i++) {

            System.out.println((i + 1) + ". " + flights.get(i));
        }
    }

    private static Flight selectFlight(List<Flight> flights) {

        System.out.print("Select Flight: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return null;
        }

        if (choice <= 0 || choice > flights.size()) {
            return null;
        }

        return flights.get(choice - 1);
    }

    private static Passenger createPassenger(User activeUser) {

        return new Passenger(
                101,
                null,
                "Mr.",
                activeUser.getFullName().split(" ")[0],
                "user",
                LocalDate.of(1990, 5, 15),
                "Male",
                "ADULT"
        );
    }

    private static TripType chooseTripType() {

        System.out.println("1. VIP (+150)");
        System.out.println("2. EXPRESS (+50)");
        System.out.println("3. REGULAR (+0)");
        System.out.println("4. STANDBY (-20)");

        String choice = scanner.nextLine();

        switch (choice) {

            case "1":
                return TripType.VIP;

            case "2":
                return TripType.EXPRESS;

            case "4":
                return TripType.STANDBY;

            default:
                return TripType.REGULAR;
        }
    }

    private static void processBookingQueue(User activeUser) {

        if (activeUser.getRole().equals("ADMIN")) {

            bookingManager.processQueue();

        } else {

            System.out.println("ACCESS DENIED");
        }
    }

    private static void logout() {

        authManager.logout();

        System.out.println("Logged out successfully.");
    }

    public static void main(String[] args) {

        System.out.println("AIRLINE RESERVATION SYSTEM");

        User activeUser = loginFlow();

        if (activeUser != null) {
            showMainMenu(activeUser);
        }

        scanner.close();
    }
}