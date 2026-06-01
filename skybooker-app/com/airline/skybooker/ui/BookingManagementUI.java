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
 * Command-line interface for passengers to manage their existing bookings.
 * Handles cancellations, seat modifications, and passenger detail updates.
 */
public class BookingManagementUI {
    private final BookingManager bookingManager;
    private final Scanner scanner;
    private final SeatService seatService;

    /**
     * Constructs the booking management interface with necessary services.
     *
     * @param scanner the input reader for capturing user commands
     * @param seatService the service handling seat reassignment operations
     */
    public BookingManagementUI(Scanner scanner, SeatService seatService) {
        this.scanner = scanner;
        this.seatService = seatService;
        this.bookingManager = BookingManager.getInstance();
    }

    /**
     * Retrieves and displays all bookings associated with the given passenger.
     * Allows selection of a specific booking for further management.
     *
     * @param passenger the authenticated passenger whose bookings to display
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
     * Presents options to modify a selected booking, checking operational rules before applying changes.
     *
     * @param booking the target booking record to manage
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
     * Executes a partial cancellation for a specific passenger within a larger booking itinerary.
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
     * Collects updated personal information or meal preferences for a specific passenger on the booking.
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
     * Orchestrates a seat reassignment by presenting the current map and updating the passenger record.
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
