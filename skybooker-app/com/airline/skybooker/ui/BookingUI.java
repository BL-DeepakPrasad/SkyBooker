package com.airline.skybooker.ui;

import com.airline.skybooker.managers.BookingManager;
import com.airline.skybooker.managers.PaymentManager;
import com.airline.skybooker.payments.PaymentStrategy;
import com.airline.skybooker.payments.CreditCardPayment;
import com.airline.skybooker.payments.UPIPayment;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.exception.SeatLockException;
import java.util.Scanner;

public class BookingUI {
    private final BookingManager bookingManager;
    private final PaymentManager paymentManager;
    private final SeatService seatService;
    private final Scanner scanner;

    public BookingUI(Scanner scanner, SeatService seatService) {
        this.scanner = scanner;
        this.bookingManager = BookingManager.getInstance();
        this.paymentManager = PaymentManager.getInstance();
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
                    System.out.println("\n[State: " + booking.getStatus() + "] Proceed to payment terminal.");
                    handlePaymentPhase(booking, flight.getBasePrice());
                }
            } catch (SeatLockException ex) {
                System.out.println("FAILED: " + ex.getMessage());
                booking.cancel();
                System.out.println("[State: " + booking.getStatus() + "]");
            }
        }
    }

    private void handlePaymentPhase(Booking booking, double amount) {
        System.out.println("Total Fare: $" + amount);
        System.out.println("Select Payment Strategy:");
        System.out.println("1. Credit/Debit Card");
        System.out.println("2. UPI / NetBanking");
        System.out.print("Enter choice (1/2): ");
        String choice = scanner.nextLine().trim();

        PaymentStrategy strategy = null;

        if (choice.equals("1")) {
            System.out.print("Enter 16-digit Card Number: ");
            String card = scanner.nextLine().trim();
            System.out.print("Enter Cardholder Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter CVV: ");
            String cvv = scanner.nextLine().trim();
            strategy = new CreditCardPayment(card, name, cvv);
        } else if (choice.equals("2")) {
            System.out.print("Enter UPI ID (e.g., name@bank): ");
            String upi = scanner.nextLine().trim();
            strategy = new UPIPayment(upi);
        } else {
            System.out.println("Invalid payment method.");
            booking.cancel();
            return;
        }

        boolean success = paymentManager.processTransaction(strategy, amount);
        if (success) {
            booking.nextState(); // Transitions to CONFIRMED
            System.out.println("\nE-TICKET GENERATED! PNR: " + booking.getPnrCode());
            System.out.println("[State: " + booking.getStatus() + "]");
        } else {
            booking.cancel();
            System.out.println("[State: " + booking.getStatus() + "]");
        }
    }
}
