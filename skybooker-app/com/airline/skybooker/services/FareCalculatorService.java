package com.airline.skybooker.services;

import com.airline.skybooker.models.Booking;
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
    public double calculateFinalFare(Booking booking, double baseFare, boolean isExpress, String promoCode) {
        double finalAmount = baseFare;
        
        if (isExpress) {
            booking.setPriority(BookingPriority.EXPRESS);
            finalAmount += 25.0; // Express Fee
        } else {
            booking.setPriority(BookingPriority.REGULAR);
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
