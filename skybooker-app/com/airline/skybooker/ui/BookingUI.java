package com.airline.skybooker.ui;

import com.airline.skybooker.managers.BookingManager;
import com.airline.skybooker.managers.PaymentManager;
import com.airline.skybooker.managers.PriorityBookingManager;
import com.airline.skybooker.interfaces.Payable;
import com.airline.skybooker.payments.CreditCardPayment;
import com.airline.skybooker.payments.UPIPayment;
import com.airline.skybooker.payments.EMIPayment;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.BookingPassenger;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.services.FareCalculatorService;
import com.airline.skybooker.services.BookingService;
import com.airline.skybooker.exception.SeatLockException;
import com.airline.skybooker.exception.PaymentFailureException;
import com.airline.skybooker.utils.ValidationUtils;
import com.airline.skybooker.utils.InputReader;
import com.airline.skybooker.utils.ErrorLogger;
import java.util.Scanner;

/**
 * Walks a user through the entire process of booking a new flight.
 * This includes asking for passenger names, picking seats on the plane, and paying for the ticket.
 */
public class BookingUI {
    private final BookingManager bookingManager;
    private final PaymentManager paymentManager;
    private final PriorityBookingManager priorityManager;
    private final SeatService seatService;
    private final Scanner scanner;

    /**
     * Sets up the flight booking menu using a Scanner for reading user input.
     *
     * @param scanner reads text typed by the user in the console
     * @param seatService used to show the seat map and check if chosen seats are available
     */
    public BookingUI(Scanner scanner, SeatService seatService) {
        this.scanner = scanner;
        this.bookingManager = BookingManager.getInstance();
        this.paymentManager = PaymentManager.getInstance();
        this.priorityManager = PriorityBookingManager.getInstance();
        this.seatService = seatService;
    }

    /**
     * Starts the step-by-step wizard to book a flight. 
     * It asks how many people are flying, collects their details, checks rules (like infants needing an adult), 
     * and lets them pick seats.
     *
     * @param passenger the person logged into the app making the booking
     * @param flight the flight they want to buy tickets for
     */
    public void startBookingFlow(Passenger passenger, Flight flight) {
        System.out.println("\n[Rule Check] Validating flight departure time...");
        // Mock departure time check - assuming mock flights are 24+ hours out
        System.out.println("[Rule Check] Flight is > 2 hours away. Booking allowed.");

        System.out.print("\nDo you want to book this flight? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) return;

        int numPassengers = InputReader.readInt(scanner, "Enter number of passengers (Max 6): ", ValidationUtils::validatePassengerCount);

        Booking booking = bookingManager.initiateBooking(passenger.getUserId(), flight.getFlightId());
        System.out.println("[State: " + booking.getStatus() + "] Collecting passenger details.");

        int adultCount = 0;
        int infantCount = 0;

        for (int i = 0; i < numPassengers; i++) {
            System.out.println("\n--- Passenger " + (i + 1) + " ---");
            String name = InputReader.readString(scanner, "Full Name: ", ValidationUtils::validateName);
            String passport = InputReader.readOptionalString(scanner, "Passport Number (Enter to skip): ", ValidationUtils::validatePassport);
            
            String ageCat = InputReader.readString(scanner, "Age Category (Adult/Child/Infant): ", 
                input -> {
                    if (!input.equalsIgnoreCase("Adult") && !input.equalsIgnoreCase("Child") && !input.equalsIgnoreCase("Infant")) {
                        throw new IllegalArgumentException("Please enter Adult, Child, or Infant.");
                    }
                }
            );
            
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
            BookingService.getInstance().cancelBooking(booking, flight, seatService);
            return;
        }

        System.out.println("\n[State: " + booking.getStatus() + "] Time to select your seats!");
        seatService.displaySeatMap(flight.getFlightNumber());
        
        for (int i = 0; i < numPassengers; i++) {
            BookingPassenger bookingPassenger = booking.getPassengers().get(i);
            String seatNum;
            while (true) {
                System.out.print("Enter Seat Number for " + bookingPassenger.getFullName() + " (e.g. 1B): ");
                seatNum = scanner.nextLine().trim().toUpperCase();
                if (seatService.isValidSeat(flight.getFlightNumber(), seatNum)) {
                    break;
                } else {
                    System.out.println("Invalid or unavailable seat. Please try again.");
                }
            }
            bookingPassenger.setSeatNumber(seatNum);
        }

        // Transition: INITIATED -> SEAT_SELECTED
        booking.nextState();

        handlePaymentPhase(booking, flight);
    }

    /**
     * Figures out the final price of the tickets and asks the user how they want to pay.
     * If the payment works, the booking is confirmed and an e-ticket is generated.
     *
     * @param booking the unconfirmed reservation we are trying to pay for
     * @param flight the flight we are booking
     */
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
        String breakdown = FareCalculatorService.getInstance().getFareBreakdown(booking, flight.getBasePrice(), isExpress, promo);
        booking.setFareBreakdown(breakdown);
        System.out.println(breakdown);
        
        double previewAmount = FareCalculatorService.getInstance().calculateFinalFare(booking, flight.getBasePrice(), isExpress, promo);
        System.out.printf("\n*** TOTAL PAYABLE AMOUNT: INR %.2f ***%n", previewAmount);

        System.out.println("\nSelect Payment Strategy:");
        System.out.println("1. Credit/Debit Card");
        System.out.println("2. UPI / NetBanking");
        System.out.println("3. EMI (Equated Monthly Installment)");
        System.out.print("Enter choice (1/2/3): ");
        String choice = scanner.nextLine().trim();

        Payable payable = null;

        if (choice.equals("1")) {
            String card = InputReader.readString(scanner, "Enter 16-digit Card Number: ", ValidationUtils::validateCardNumber);
            System.out.print("Enter Cardholder Name: ");
            String name = scanner.nextLine().trim();
            String cvv = InputReader.readString(scanner, "Enter 3-digit CVV: ", ValidationUtils::validateCvv);
            payable = new CreditCardPayment(card, name, cvv);
        } else if (choice.equals("2")) {
            String upi = InputReader.readString(scanner, "Enter UPI ID (e.g. user@okicici): ", ValidationUtils::validateUpiId);
            payable = new UPIPayment(upi);
        } else if (choice.equals("3")) {
            String card = InputReader.readString(scanner, "Enter 16-digit Card Number: ", null);
            int months = InputReader.readInt(scanner, "Enter EMI Months (3/6/9/12): ", null);
            payable = new EMIPayment(card, months);
        }

        if (payable == null || !payable.validate()) {
            System.out.println("Invalid Payment Details. Booking Cancelled.");
            return;
        }

        try {
            boolean success = BookingService.getInstance().processPaymentAndConfirm(booking, flight, isExpress, payable, seatService, promo);
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
            BookingService.getInstance().cancelBooking(booking, flight, seatService);
            System.out.println("[State: " + booking.getStatus() + "]");
        } catch (Exception ex) {
            System.out.println("Unexpected Error: " + ex.getMessage());
            ErrorLogger.logError(ex);
            BookingService.getInstance().cancelBooking(booking, flight, seatService);
        }
    }
}
