package com.airline.skybooker.ui;

import com.airline.skybooker.managers.BookingManager;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.Passenger;
import java.util.List;
import java.util.Scanner;

import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.services.BookingService;
import com.airline.skybooker.managers.FlightManager;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.BookingPassenger;

/**
 * Provides a menu for passengers to view and manage flights they have already booked.
 * Users can cancel tickets, change their seats, or update their personal details here.
 */
public class BookingManagementUI {
    private final BookingManager bookingManager;
    private final Scanner scanner;
    private final SeatService seatService;

    /**
     * Sets up the booking management menu using a Scanner for reading user input.
     *
     * @param scanner reads text typed by the user in the console
     * @param seatService helps free up or assign new seats when a booking is modified
     */
    public BookingManagementUI(Scanner scanner, SeatService seatService) {
        this.scanner = scanner;
        this.seatService = seatService;
        this.bookingManager = BookingManager.getInstance();
    }

    /**
     * Shows every single booking made in the entire system.
     * This is an admin-only feature to help manage or troubleshoot user bookings.
     *
     * @param admin the admin user currently logged in
     */
    public void displayAllSystemBookings(com.airline.skybooker.models.Admin admin) {
        System.out.println("\n============================================");
        System.out.println("            ALL SYSTEM BOOKINGS             ");
        System.out.println("============================================");

        List<Booking> allBookings = bookingManager.getAllBookings();

        if (allBookings.isEmpty()) {
            System.out.println("There are no bookings in the system.");
            System.out.println("Press Enter to return to Dashboard...");
            scanner.nextLine();
            return;
        }

        for (int i = 0; i < allBookings.size(); i++) {
            Booking b = allBookings.get(i);
            String pnr = b.getPnrCode() != null ? b.getPnrCode() : "PENDING";
            System.out.println((i + 1) + ". [PNR: " + pnr + "] UserID: " + b.getUserId() + " | FlightID: " + b.getFlightId() + " | Status: " + b.getStatus());
        }

        System.out.print("\nEnter a booking number to manage (or press Enter to go back): ");
        String input = scanner.nextLine().trim();

        if (!input.isEmpty()) {
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < allBookings.size()) {
                    manageSingleBooking(allBookings.get(index));
                } else {
                    System.out.println("Invalid selection.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }

    /**
     * Prints a list of only the bookings that belong to the currently logged-in passenger.
     * The user can then pick one to view or change.
     *
     * @param passenger the user who wants to see their bookings
     */
    public void displayMyBookings(Passenger passenger) {
        System.out.println("\n============================================");
        System.out.println("               MY BOOKINGS                  ");
        System.out.println("============================================");

        List<Booking> myBookings = bookingManager.getBookingsForUser(passenger.getUserId());

        if (myBookings.isEmpty()) {
            System.out.println("You have no bookings.");
            System.out.println("Press Enter to return to Dashboard...");
            scanner.nextLine();
            return;
        }

        for (int i = 0; i < myBookings.size(); i++) {
            Booking b = myBookings.get(i);
            String pnr = b.getPnrCode() != null ? b.getPnrCode() : "PENDING";
            System.out.println((i + 1) + ". [PNR: " + pnr + "] FlightID: " + b.getFlightId() + " | Status: " + b.getStatus());
        }

        System.out.print("\nEnter a booking number to manage (or press Enter to go back): ");
        String input = scanner.nextLine().trim();

        if (!input.isEmpty()) {
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < myBookings.size()) {
                    manageSingleBooking(myBookings.get(index));
                } else {
                    System.out.println("Invalid selection.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }

    /**
     * Shows a detailed view of a single booking and lets the user make changes if it isn't already cancelled.
     * Options include cancelling the whole trip, just one passenger, or picking a new seat.
     *
     * @param booking the specific ticket reservation we are looking at
     */
    private void manageSingleBooking(Booking booking) {
        Flight flight = FlightManager.getInstance().getFlightById(booking.getFlightId()).orElse(null);
        if (flight == null) return;

        while(true) {
            System.out.println("\n--- MANAGE BOOKING ---");
            System.out.println("PNR: " + booking.getPnrCode());
            System.out.println("Status: " + booking.getStatus());
            System.out.printf("Total Fare Paid: INR %.2f%n", booking.getTotalFare());
            
            boolean isBookingCancelled = booking.getStatus().equals("CANCELLED") || booking.getStatus().equals("REFUNDED");

            System.out.println("\nPassengers:");
            List<BookingPassenger> passengers = booking.getPassengers();
            for (int i = 0; i < passengers.size(); i++) {
                BookingPassenger bp = passengers.get(i);
                String status = (bp.isCancelled() || isBookingCancelled) ? "[CANCELLED]" : "[CONFIRMED]";
                System.out.printf("  %d. %s - Seat: %s %s%n", (i+1), bp.getFullName(), bp.getSeatNumber(), status);
            }
            
            if (isBookingCancelled) {
                System.out.println("\nThis booking is cancelled. No further modifications are allowed.");
                System.out.println("0. Go Back");
            } else {
                System.out.println("\n1. Cancel Entire Booking");
                System.out.println("2. Cancel Specific Passenger (Partial Cancellation)");
                System.out.println("3. Edit Passenger Details");
                System.out.println("4. Change Seat Assignment");
                if (booking.getFareBreakdown() != null) {
                    System.out.println("5. View Fare Breakdown");
                }
                System.out.println("0. Go Back");
            }
            
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                break;
            } else if (!isBookingCancelled) {
                if (choice.equals("1")) {
                    System.out.print("Are you sure you want to cancel this ENTIRE booking? (y/n): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                        BookingService.getInstance().cancelBooking(booking, flight, seatService);
                        System.out.println("Booking Cancelled Successfully.");
                        break;
                    }
                } else if (choice.equals("2")) {
                    handlePartialCancellation(booking, flight, passengers);
                } else if (choice.equals("3")) {
                    handleEditPassenger(booking, passengers);
                } else if (choice.equals("4")) {
                    handleChangeSeat(booking, flight, passengers);
                } else if (choice.equals("5") && booking.getFareBreakdown() != null) {
                    System.out.println(booking.getFareBreakdown());
                } else {
                    System.out.println("Invalid selection.");
                }
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    /**
     * Cancels the ticket for just one person in a group booking, without affecting the others.
     */
    private void handlePartialCancellation(Booking booking, Flight flight, List<BookingPassenger> passengers) {
        System.out.print("Enter passenger number from the list above (e.g. 1): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx >= 0 && idx < passengers.size() && !passengers.get(idx).isCancelled()) {
                BookingService.getInstance().cancelSpecificPassenger(booking, flight, idx, seatService);
                System.out.println("Passenger Cancelled.");
            } else {
                System.out.println("Invalid selection or already cancelled.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    /**
     * Asks the user for new details, like fixing a misspelled name or adding a meal, and updates the passenger.
     */
    private void handleEditPassenger(Booking booking, List<BookingPassenger> passengers) {
        System.out.print("Enter passenger number from the list above (e.g. 1): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx >= 0 && idx < passengers.size() && !passengers.get(idx).isCancelled()) {
                System.out.print("Enter new Full Name (press Enter to keep current): ");
                String name = scanner.nextLine().trim();
                System.out.print("Enter new Passport (press Enter to keep current): ");
                String passport = scanner.nextLine().trim();
                System.out.print("Add Meal Upgrade? (y/n): ");
                boolean meal = scanner.nextLine().trim().equalsIgnoreCase("y");
                
                bookingManager.modifyPassengerDetails(booking, idx, name, passport, meal);
                System.out.println("Passenger details updated successfully!");
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    /**
     * Shows the available seats on the plane and lets a passenger pick a new one, freeing up their old seat.
     */
    private void handleChangeSeat(Booking booking, Flight flight, List<BookingPassenger> passengers) {
        System.out.print("Enter passenger number from the list above (e.g. 1): ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx >= 0 && idx < passengers.size() && !passengers.get(idx).isCancelled()) {
                seatService.displaySeatMap(flight.getFlightNumber());
                System.out.print("Enter new Seat Number: ");
                String newSeat = scanner.nextLine().trim().toUpperCase();
                
                if (bookingManager.changePassengerSeat(booking, flight, idx, newSeat, seatService)) {
                    System.out.println("Seat successfully changed to " + newSeat);
                }
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }
}
