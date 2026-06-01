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
import com.airline.skybooker.exception.PaymentFailureException;
import com.airline.skybooker.utils.ValidationUtils;
import com.airline.skybooker.utils.ErrorLogger;
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
                ValidationUtils.validatePassengerCount(numPassengers);
                break;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        Booking booking = bookingManager.initiateBooking(passenger.getUserId(), flight.getFlightId());
        booking.nextState();
        System.out.println("[State: " + booking.getStatus() + "] Collecting passenger details.");

        int adultCount = 0;
        int infantCount = 0;

        for (int i = 0; i < numPassengers; i++) {
            System.out.println("\n--- Passenger " + (i + 1) + " ---");
            String name;
            while (true) {
                System.out.print("Full Name: ");
                name = scanner.nextLine().trim();
                try {
                    ValidationUtils.validateName(name);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }

            String passport;
            while (true) {
                System.out.print("Passport Number (Enter to skip): ");
                passport = scanner.nextLine().trim();
                if (passport.isEmpty()) break;
                try {
                    ValidationUtils.validatePassport(passport);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }

            String ageCat;
            while (true) {
                System.out.print("Age Category (Adult/Child/Infant): ");
                ageCat = scanner.nextLine().trim();
                if (ageCat.equalsIgnoreCase("Adult") || ageCat.equalsIgnoreCase("Child") || ageCat.equalsIgnoreCase("Infant")) {
                    break;
                }
                System.out.println("ERROR: Please enter Adult, Child, or Infant.");
            }
            
            if (ageCat.equalsIgnoreCase("Adult")) adultCount++;
            else if (ageCat.equalsIgnoreCase("Infant")) infantCount++;

            BookingPassenger bp = new BookingPassenger(name, passport, ageCat);

            // Baggage
            while (true) {
                System.out.print("Estimated Baggage Weight in kg (Standard is 15kg): ");
                String weightInput = scanner.nextLine().trim();
                if (weightInput.isEmpty()) break;
                try {
                    double weight = Double.parseDouble(weightInput);
                    if (weight < 0 || weight > 100) {
                        System.out.println("ERROR: Baggage weight must be between 0 and 100 kg.");
                        continue;
                    }
                    bp.setBaggageWeight(weight);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("ERROR: Invalid number format.");
                }
            }

            // Meal
            while (true) {
                System.out.print("Opt for Meal Upgrade (+INR 20)? (y/n): ");
                String mealInput = scanner.nextLine().trim().toLowerCase();
                if (mealInput.equals("y") || mealInput.equals("n") || mealInput.isEmpty()) {
                    bp.setMealUpgrade(mealInput.equals("y"));
                    break;
                }
                System.out.println("ERROR: Please enter 'y' or 'n'.");
            }

            booking.getPassengers().add(bp);
        }

        // Business Rule Validation: Infant must travel with adult
        if (infantCount > 0 && adultCount == 0) {
            System.out.println("\n[ERROR] Business Rule Violation: An Infant must travel with at least one Adult.");
            bookingManager.cancelBooking(booking, flight, seatService);
            return;
        }

        System.out.println("\n[State: " + booking.getStatus() + "] Time to select your seats!");
        seatService.displaySeatMap(flight.getFlightNumber());
        
        for (int i = 0; i < numPassengers; i++) {
            BookingPassenger bp = booking.getPassengers().get(i);
            String seatNum;
            while (true) {
                System.out.print("Enter Seat Number for " + bp.getFullName() + " (e.g. 1B): ");
                seatNum = scanner.nextLine().trim().toUpperCase();
                if (seatService.isValidSeat(flight.getFlightNumber(), seatNum)) {
                    break;
                } else {
                    System.out.println("Invalid or unavailable seat. Please try again.");
                }
            }
            bp.setSeatNumber(seatNum);
        }

        handlePaymentPhase(booking, flight);
    }

    private void handlePaymentPhase(Booking booking, Flight flight) {
        System.out.println("\n[UPGRADE OPPORTUNITY]");
        System.out.print("Opt for EXPRESS Booking for an additional INR 25 fee? (Faster Processing) (y/n): ");
        boolean isExpress = scanner.nextLine().trim().equalsIgnoreCase("y");

        System.out.print("\nDo you have a Promotional Code? (Press Enter to skip): ");
        String promo = scanner.nextLine().trim();
        if (promo.equalsIgnoreCase("SKYBOOKER20")) {
            System.out.println("[PROMO] Promo code SKYBOOKER20 applied! 20% Discount.");
        }

        System.out.println("\nCalculating complex dynamic fares based on age, baggage, and seat selections...");
        boolean isDomestic = flight.getOrigin().getCountry().equalsIgnoreCase(flight.getDestination().getCountry());
        
        String breakdown = com.airline.skybooker.services.FareCalculatorService.getInstance().getFareBreakdown(booking, flight.getBasePrice(), isExpress, promo, isDomestic);
        booking.setFareBreakdown(breakdown);
        System.out.println(breakdown);
        
        double previewAmount = com.airline.skybooker.services.FareCalculatorService.getInstance().calculateFinalFare(booking, flight.getBasePrice(), isExpress, promo, isDomestic);
        System.out.printf("\n*** TOTAL PAYABLE AMOUNT: INR %.2f ***%n", previewAmount);

        System.out.println("\nSelect Payment Strategy:");
        System.out.println("1. Credit/Debit Card");
        System.out.println("2. UPI / NetBanking");
        System.out.println("3. EMI (Equated Monthly Installment)");
        System.out.print("Enter choice (1/2/3): ");
        String choice = scanner.nextLine().trim();

        PaymentStrategy strategy = null;

        if (choice.equals("1")) {
            String card;
            while (true) {
                System.out.print("Enter 16-digit Card Number: ");
                card = scanner.nextLine().trim();
                try {
                    ValidationUtils.validateCardNumber(card);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
            System.out.print("Enter Cardholder Name: ");
            String name = scanner.nextLine().trim();
            
            String cvv;
            while (true) {
                System.out.print("Enter 3-digit CVV: ");
                cvv = scanner.nextLine().trim();
                try {
                    ValidationUtils.validateCvv(cvv);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
            strategy = new CreditCardPayment(card, name, cvv);
        } else if (choice.equals("2")) {
            String upi;
            while (true) {
                System.out.print("Enter UPI ID (e.g., name@bank): ");
                upi = scanner.nextLine().trim();
                try {
                    ValidationUtils.validateUpiId(upi);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
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
            bookingManager.cancelBooking(booking, flight, seatService);
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
                System.out.printf("Total Paid:   INR %.2f%n", booking.getTotalFare());
                System.out.println("====================================");
            } else {
                System.out.println("Payment failed. Booking cancelled.");
                System.out.println("[State: " + booking.getStatus() + "]");
            }
        } catch (SeatLockException | PaymentFailureException ex) {
            System.out.println("FAILED: " + ex.getMessage());
            ErrorLogger.logError(ex);
            bookingManager.cancelBooking(booking, flight, seatService);
            System.out.println("[State: " + booking.getStatus() + "]");
        } catch (Exception ex) {
            System.out.println("Unexpected Error: " + ex.getMessage());
            ErrorLogger.logError(ex);
            bookingManager.cancelBooking(booking, flight, seatService);
        }
    }
}
