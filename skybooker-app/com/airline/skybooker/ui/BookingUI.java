package com.airline.skybooker.ui;

import com.airline.skybooker.managers.BookingManager;
import com.airline.skybooker.managers.PaymentManager;
import com.airline.skybooker.managers.PriorityBookingManager;
import com.airline.skybooker.payments.PaymentStrategy;
import com.airline.skybooker.payments.CreditCardPayment;
import com.airline.skybooker.payments.UPIPayment;
import com.airline.skybooker.payments.EMIPayment;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.enums.BookingPriority;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.exception.SeatLockException;
import java.util.Scanner;

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
        System.out.print("\nDo you want to book this flight? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            Booking booking = bookingManager.initiateBooking(passenger.getUserId(), flight.getFlightId());
            booking.nextState();
            
            System.out.println("[State: " + booking.getStatus() + "] Please confirm passenger details.");
            System.out.println("Name: " + passenger.getFullName() + ", Passport: " + passenger.getPassportNumber());
            
            System.out.println("\n[State: " + booking.getStatus() + "] Time to select your seat!");
            seatService.displaySeatMap(flight.getFlightNumber());
            System.out.print("Enter Seat Number to lock (e.g. 1B): ");
            String seatNum = scanner.nextLine().trim();
            
            handlePaymentPhase(booking, flight.getFlightNumber(), seatNum, flight.getBasePrice());
        }
    }

    private void handlePaymentPhase(Booking booking, String flightNumber, String seatNum, double amount) {
        System.out.println("\n[UPGRADE OPPORTUNITY]");
        System.out.print("Opt for EXPRESS Booking for an additional $25 fee? (Faster Processing) (y/n): ");
        boolean isExpress = scanner.nextLine().trim().equalsIgnoreCase("y");

        double displayAmount = amount + (isExpress ? 25.0 : 0.0);
        
        System.out.print("\nDo you have a Promotional Code? (Press Enter to skip): ");
        String promo = scanner.nextLine().trim();
        if (promo.equalsIgnoreCase("SKYBOOKER20")) {
            System.out.println("[PROMO] Promo code SKYBOOKER20 applied! 20% Discount.");
            displayAmount = displayAmount * 0.80;
        } else if (!promo.isEmpty()) {
            System.out.println("[PROMO] Invalid promo code.");
        }

        System.out.printf("%n[BILLING] Total Fare to be charged: $%.2f%n", displayAmount);

        System.out.println("Select Payment Strategy:");
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
            } catch (NumberFormatException e) {
                // fall back to validation failing
            }
            strategy = new EMIPayment(card, months);
        } else {
            System.out.println("Invalid payment method.");
            bookingManager.cancelBooking(booking);
            return;
        }

        try {
            boolean success = bookingManager.processPaymentAndConfirm(booking, flightNumber, seatNum, isExpress, strategy, amount, seatService, promo);
            if (success) {
                System.out.println("\n====================================");
                System.out.println("          E-TICKET GENERATED        ");
                System.out.println("====================================");
                System.out.println("PNR:          " + booking.getPnrCode());
                System.out.println("Status:       " + booking.getStatus());
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
