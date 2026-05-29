package com.airline.skybooker.ui;

import com.airline.skybooker.managers.BookingManager;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.exception.SeatLockException;
import java.util.Scanner;

public class BookingUI {
    private final BookingManager bookingManager;
    private final SeatService seatService;
    private final Scanner scanner;

    public BookingUI(Scanner scanner, SeatService seatService) {
        this.scanner = scanner;
        this.bookingManager = BookingManager.getInstance();
        this.seatService = seatService;
    }

    public void startBookingFlow(Passenger passenger, Flight flight) {
        System.out.print("\nDo you want to book this flight? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            Booking booking = bookingManager.initiateBooking(passenger.getUserId(), flight.getFlightId());
            
            // Transition: INITIATED -> PASSENGER_DETAILS
            booking.nextState();
            System.out.println("[State: " + booking.getStatus() + "] Please confirm passenger details.");
            System.out.println("Name: " + passenger.getFullName() + ", Passport: " + passenger.getPassportNumber());
            
            // Transition: PASSENGER_DETAILS -> SEAT_SELECTED
            booking.nextState();
            System.out.println("\n[State: " + booking.getStatus() + "] Time to select your seat!");
            seatService.displaySeatMap(flight.getFlightNumber());
            System.out.print("Enter Seat Number to lock (e.g. 1B): ");
            String seatNum = scanner.nextLine().trim();
            try {
                if (seatService.lockSeat(flight.getFlightNumber(), seatNum)) {
                    System.out.println("SUCCESS: Seat " + seatNum + " has been locked.");
                    // Transition: SEAT_SELECTED -> PAYMENT_PENDING
                    booking.nextState();
                    System.out.println("[State: " + booking.getStatus() + "] Proceed to payment terminal.");
                }
            } catch (SeatLockException ex) {
                System.out.println("FAILED: " + ex.getMessage());
                booking.cancel();
                System.out.println("[State: " + booking.getStatus() + "]");
            }
        }
    }
}
