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
     * Calculates the refund amount and cancellation penalty.
     * Returns an array where [0] is the refund amount, and [1] is the penalty applied.
     */
    public double[] calculateRefundAndPenalty(double totalFarePaid) {
        double penalty = totalFarePaid * 0.20; // 20% cancellation penalty
        double refundAmount = totalFarePaid - penalty;
        return new double[]{refundAmount, penalty};
    }
}
