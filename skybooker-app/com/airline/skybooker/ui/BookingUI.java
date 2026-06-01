package com.airline.skybooker.ui;

import com.airline.skybooker.managers.BookingManager;
import com.airline.skybooker.managers.PaymentManager;
import com.airline.skybooker.managers.PriorityBookingManager;
import com.airline.skybooker.payments.PaymentStrategy;
import com.airline.skybooker.payments.CreditCardPayment;
import com.airline.skybooker.payments.UPIPayment;
import com.airline.skybooker.payments.EMIPayment;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.BookingPassenger;
import com.airline.skybooker.enums.BookingPriority;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.exception.SeatLockException;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class BookingUI {
    private final BookingManager bookingManager;
    private final PaymentManager paymentManager;
    private final PriorityBookingManager priorityManager;
    private final SeatService seatService;
    private final Scanner scanner;

    public BookingUI(Scanner scanner, SeatService seatService) {
        this.scanner = scanner;
        this.bookingManager = BookingManager.getInstance();
        this.paymentManager = PaymentManager.getInstance();
        this.priorityManager = PriorityBookingManager.getInstance();
        this.seatService = seatService;
    }

    public void startBookingFlow(Passenger passenger, Flight flight) {
        System.out.println("\n[Rule Check] Validating flight departure time...");
        // Mock departure time check - assuming mock flights are 24+ hours out
        System.out.println("[Rule Check] Flight is > 2 hours away. Booking allowed.");

        System.out.print("\nDo you want to book this flight? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) return;

        int numPassengers = 0;
        while (true) {
            System.out.print("Enter number of passengers (Max 6): ");
            try {
                numPassengers = Integer.parseInt(scanner.nextLine().trim());
                if (numPassengers >= 1 && numPassengers <= 6) break;
                System.out.println("Error: Must be between 1 and 6.");
            } catch (Exception e) {
                System.out.println("Invalid number.");
            }
        }

        Booking booking = bookingManager.initiateBooking(passenger.getUserId(), flight.getFlightId());
        booking.nextState();
        System.out.println("[State: " + booking.getStatus() + "] Collecting passenger details.");

        int adultCount = 0;
        int infantCount = 0;

        for (int i = 0; i < numPassengers; i++) {
            System.out.println("\n--- Passenger " + (i + 1) + " ---");
            System.out.print("Full Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Passport Number (Enter to skip): ");
            String passport = scanner.nextLine().trim();
            System.out.print("Age Category (Adult/Child/Infant): ");
            String ageCat = scanner.nextLine().trim();
            
            if (ageCat.equalsIgnoreCase("Adult")) adultCount++;
            else if (ageCat.equalsIgnoreCase("Infant")) infantCount++;

            BookingPassenger bp = new BookingPassenger(name, passport, ageCat);

            // Baggage
            System.out.print("Estimated Baggage Weight in kg (Standard is 15kg): ");
            try {
                double weight = Double.parseDouble(scanner.nextLine().trim());
                bp.setBaggageWeight(weight);
            } catch (Exception e) {}

            // Meal
            System.out.print("Opt for Meal Upgrade (+$20)? (y/n): ");
            bp.setMealUpgrade(scanner.nextLine().trim().equalsIgnoreCase("y"));

            booking.getPassengers().add(bp);
        }

        // Business Rule Validation: Infant must travel with adult
        if (infantCount > 0 && adultCount == 0) {
            System.out.println("\n[ERROR] Business Rule Violation: An Infant must travel with at least one Adult.");
            bookingManager.cancelBooking(booking);
            return;
        }

        System.out.println("\n[State: " + booking.getStatus() + "] Time to select your seats!");
        seatService.displaySeatMap(flight.getFlightNumber());
        
        for (int i = 0; i < numPassengers; i++) {
            BookingPassenger bp = booking.getPassengers().get(i);
            System.out.print("Enter Seat Number for " + bp.getFullName() + " (e.g. 1B): ");
            String seatNum = scanner.nextLine().trim();
            bp.setSeatNumber(seatNum);
        }

        handlePaymentPhase(booking, flight);
    }

    private void handlePaymentPhase(Booking booking, Flight flight) {
        System.out.println("\n[UPGRADE OPPORTUNITY]");
        System.out.print("Opt for EXPRESS Booking for an additional $25 fee? (Faster Processing) (y/n): ");
        boolean isExpress = scanner.nextLine().trim().equalsIgnoreCase("y");

        System.out.print("\nDo you have a Promotional Code? (Press Enter to skip): ");
        String promo = scanner.nextLine().trim();
        if (promo.equalsIgnoreCase("SKYBOOKER20")) {
            System.out.println("[PROMO] Promo code SKYBOOKER20 applied! 20% Discount.");
        }

        System.out.println("\nCalculating complex dynamic fares based on age, baggage, and seat selections...");

        System.out.println("\nSelect Payment Strategy:");
        System.out.println("1. Credit/Debit Card");
        System.out.println("2. UPI / NetBanking");
        System.out.println("3. EMI (Equated Monthly Installment)");
        System.out.print("Enter choice (1/2/3): ");
        String choice = scanner.nextLine().trim();

        PaymentStrategy strategy = null;

        if (choice.equals("1")) {
            System.out.print("Enter 16-digit Card Number: ");
            String card = scanner.nextLine().trim();
            System.out.print("Enter Cardholder Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter 3-digit CVV: ");
            String cvv = scanner.nextLine().trim();
            strategy = new CreditCardPayment(card, name, cvv);
        } else if (choice.equals("2")) {
            System.out.print("Enter UPI ID (e.g., name@bank): ");
            String upi = scanner.nextLine().trim();
            strategy = new UPIPayment(upi);
        } else if (choice.equals("3")) {
            System.out.print("Enter 16-digit Card Number for EMI: ");
            String card = scanner.nextLine().trim();
            System.out.print("Enter Tenure (3, 6, or 12 months): ");
            int months = 3;
            try {
                months = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {}
            strategy = new EMIPayment(card, months);
        } else {
            System.out.println("Invalid payment method.");
            bookingManager.cancelBooking(booking);
            return;
        }

        try {
            boolean success = bookingManager.processPaymentAndConfirm(booking, flight, isExpress, strategy, seatService, promo);
            if (success) {
                System.out.println("\n====================================");
                System.out.println("          E-TICKET GENERATED        ");
                System.out.println("====================================");
                System.out.println("PNR:          " + booking.getPnrCode());
                System.out.println("Status:       " + booking.getStatus());
                System.out.println("Passengers:   " + booking.getPassengers().size());
                System.out.printf("Total Paid:   $%.2f%n", booking.getTotalFare());
                System.out.println("====================================");
            } else {
                System.out.println("Payment failed. Booking cancelled.");
                System.out.println("[State: " + booking.getStatus() + "]");
            }
        } catch (SeatLockException ex) {
            System.out.println("FAILED: " + ex.getMessage());
            bookingManager.cancelBooking(booking);
            System.out.println("[State: " + booking.getStatus() + "]");
        }
    }
}
