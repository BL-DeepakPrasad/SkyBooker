package com.airline.skybooker.ui;

import com.airline.skybooker.managers.BookingManager;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.Passenger;
import java.util.List;
import java.util.Scanner;

public class BookingManagementUI {
    private final BookingManager bookingManager;
    private final Scanner scanner;

    public BookingManagementUI(Scanner scanner) {
        this.scanner = scanner;
        this.bookingManager = BookingManager.getInstance();
    }

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

    private void manageSingleBooking(Booking booking) {
        System.out.println("\n--- MANAGE BOOKING ---");
        System.out.println("PNR: " + booking.getPnrCode());
        System.out.println("Status: " + booking.getStatus());
        System.out.printf("Total Fare Paid: $%.2f%n", booking.getTotalFare());
        System.out.println("1. Cancel Booking");
        System.out.println("2. Go Back");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            System.out.print("Are you sure you want to cancel this booking? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                bookingManager.cancelBooking(booking);
                System.out.println("Cancellation request processed. Current Status: " + booking.getStatus());
            }
        }
    }
}
