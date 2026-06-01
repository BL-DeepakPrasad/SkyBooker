package com.airline.skybooker.services;

import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.BookingPassenger;
import com.airline.skybooker.enums.BookingPriority;

/**
 * Service responsible for all pricing and penalty calculations.
 * Extracts pricing business rules out of the BookingManager to enforce SRP.
 */
public class FareCalculatorService {

    private static volatile FareCalculatorService instance;

    private FareCalculatorService() {}

    public static FareCalculatorService getInstance() {
        if (instance == null) {
            synchronized (FareCalculatorService.class) {
                if (instance == null) {
                    instance = new FareCalculatorService();
                }
            }
        }
        return instance;
    }

    /**
     * Calculates the final payable amount before a booking is confirmed.
     * Applies priority fees and promotional discounts.
     */
    public double calculateFinalFare(Booking booking, double baseFare, boolean isExpress, String promoCode, boolean isDomestic) {
        double totalPassengerFare = 0.0;
        
        for (BookingPassenger bp : booking.getPassengers()) {
            double passengerFare = baseFare;
            
            // Age discounts
            if (bp.getAgeCategory().equalsIgnoreCase("Child")) {
                passengerFare *= 0.75; // 25% off for children
            } else if (bp.getAgeCategory().equalsIgnoreCase("Infant")) {
                passengerFare *= 0.10; // 90% off for infants
            }
            
            // Baggage: INR 10 per kg over 15kg
            if (bp.getBaggageWeight() > 15.0) {
                passengerFare += (bp.getBaggageWeight() - 15.0) * 10.0;
            }
            
            // Meal upgrade
            if (bp.hasMealUpgrade()) {
                passengerFare += 20.0;
            }
            
            // Seat Selection: Premium for A, C, D, F
            if (bp.getSeatNumber() != null && !bp.getSeatNumber().isEmpty()) {
                char seatLetter = bp.getSeatNumber().charAt(bp.getSeatNumber().length() - 1);
                if (seatLetter == 'A' || seatLetter == 'F' || seatLetter == 'C' || seatLetter == 'D') {
                    passengerFare += 15.0;
                }
            }
            
            totalPassengerFare += passengerFare;
        }

        double finalAmount = totalPassengerFare;
        
        // Surcharges per booking
        finalAmount += 20.0; // Airport charges
        finalAmount += 15.0; // Fuel surcharge

        if (isExpress) {
            booking.setPriority(BookingPriority.EXPRESS);
            finalAmount += 25.0; // Express Fee
        } else {
            booking.setPriority(BookingPriority.REGULAR);
        }
        
        // GST for domestic flights (5%)
        if (isDomestic) {
            finalAmount += finalAmount * 0.05;
        }
        
        if (promoCode != null && promoCode.equalsIgnoreCase("SKYBOOKER20")) {
            finalAmount = finalAmount * 0.80; // 20% Discount
        }
        
        return finalAmount;
    }

    /**
     * Generates a receipt-style breakdown of the fare for the UI.
     */
    public String getFareBreakdown(Booking booking, double baseFare, boolean isExpress, String promoCode, boolean isDomestic) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n============================================\n");
        sb.append("               FARE BREAKDOWN                 \n");
        sb.append("============================================\n");
        
        double totalPassengerFare = 0.0;
        
        for (int i = 0; i < booking.getPassengers().size(); i++) {
            BookingPassenger bp = booking.getPassengers().get(i);
            sb.append(String.format("Passenger %d (%s):%n", (i+1), bp.getFullName()));
            
            double passengerFare = baseFare;
            sb.append(String.format("  Base Fare: INR %.2f%n", baseFare));
            
            if (bp.getAgeCategory().equalsIgnoreCase("Child")) {
                passengerFare *= 0.75;
                sb.append(String.format("  Child Discount (25%%): -INR %.2f%n", baseFare * 0.25));
            } else if (bp.getAgeCategory().equalsIgnoreCase("Infant")) {
                passengerFare *= 0.10;
                sb.append(String.format("  Infant Discount (90%%): -INR %.2f%n", baseFare * 0.90));
            }
            
            if (bp.getBaggageWeight() > 15.0) {
                double excess = (bp.getBaggageWeight() - 15.0) * 10.0;
                passengerFare += excess;
                sb.append(String.format("  Excess Baggage (%.1f kg): +INR %.2f%n", (bp.getBaggageWeight() - 15.0), excess));
            }
            
            if (bp.hasMealUpgrade()) {
                passengerFare += 20.0;
                sb.append("  Meal Upgrade: +INR 20.00\n");
            }
            
            if (bp.getSeatNumber() != null && !bp.getSeatNumber().isEmpty()) {
                char seatLetter = bp.getSeatNumber().charAt(bp.getSeatNumber().length() - 1);
                if (seatLetter == 'A' || seatLetter == 'F' || seatLetter == 'C' || seatLetter == 'D') {
                    passengerFare += 15.0;
                    sb.append(String.format("  Premium Seat (%s): +INR 15.00%n", bp.getSeatNumber()));
                }
            }
            sb.append(String.format("  Subtotal: INR %.2f%n", passengerFare));
            totalPassengerFare += passengerFare;
        }

        sb.append("--------------------------------------------\n");
        sb.append(String.format("Passengers Subtotal: INR %.2f%n", totalPassengerFare));
        
        double finalAmount = totalPassengerFare;
        
        sb.append("Airport Charges: +INR 20.00\n");
        finalAmount += 20.0;
        
        sb.append("Fuel Surcharge: +INR 15.00\n");
        finalAmount += 15.0;

        if (isExpress) {
            sb.append("Express Booking Fee: +INR 25.00\n");
            finalAmount += 25.0;
        }
        
        if (isDomestic) {
            double gst = finalAmount * 0.05;
            sb.append(String.format("GST (5%%): +INR %.2f%n", gst));
            finalAmount += gst;
        }
        
        if (promoCode != null && promoCode.equalsIgnoreCase("SKYBOOKER20")) {
            double discount = finalAmount * 0.20;
            sb.append(String.format("Promo Discount (20%%): -INR %.2f%n", discount));
            finalAmount -= discount;
        }
        
        sb.append("============================================");
        return sb.toString();
    }

    /**
     * Calculates the refund amount and cancellation penalty.
     * Returns an array where [0] is the refund amount, and [1] is the penalty applied.
     */
    public double[] calculateRefundAndPenalty(double totalFarePaid) {
        double penalty = totalFarePaid * 0.20; // 20% cancellation penalty
        double refundAmount = totalFarePaid - penalty;
        return new double[]{refundAmount, penalty};
    }
}
