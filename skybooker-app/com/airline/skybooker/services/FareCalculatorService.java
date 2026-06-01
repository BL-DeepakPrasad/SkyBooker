package com.airline.skybooker.services;

import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.BookingPassenger;
import com.airline.skybooker.enums.BookingPriority;
import com.airline.skybooker.constants.AppConstants;

/**
 * Centralized business logic component for dynamically determining pricing, surcharges, and penalties.
 * Enforces the Single Responsibility Principle by decoupling financial rule evaluation from core reservation flows.
 */
public class FareCalculatorService {

    private static volatile FareCalculatorService instance;

    private FareCalculatorService() {}

    /**
     * Retrieves the singleton instance of the FareCalculatorService.
     * Guaranteed to return a single, thread-safe instance across the application lifecycle.
     *
     * @return the singleton instance of the FareCalculatorService
     */
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
     * Computes the total transaction cost by aggregating base fares, priority surcharges, taxes, and promotional deductions.
     * 
     * @param booking    The reservation context containing passenger details and selected add-ons
     * @param baseFare   The standard ticket price per seat before modifications
     * @param isExpress  Flag dictating whether to apply expedited processing fees
     * @param promoCode  The optional coupon code to evaluate for discounts
     * @param isDomestic Flag indicating if regional taxation rules apply
     * @return The final aggregated price to be charged to the customer
     */
    public double calculateFinalFare(Booking booking, double baseFare, boolean isExpress, String promoCode, boolean isDomestic) {
        double totalPassengerFare = 0.0;
        
        for (BookingPassenger bp : booking.getPassengers()) {
            double passengerFare = baseFare;
            
            // Age discounts
            if (bp.getAgeCategory().equalsIgnoreCase("Child")) {
                passengerFare *= AppConstants.CHILD_DISCOUNT_MULTIPLIER;
            } else if (bp.getAgeCategory().equalsIgnoreCase("Infant")) {
                passengerFare *= AppConstants.INFANT_DISCOUNT_MULTIPLIER;
            }
            
            // Baggage: INR 10 per kg over 15kg
            if (bp.getBaggageWeight() > AppConstants.FREE_BAGGAGE_ALLOWANCE_KG) {
                passengerFare += (bp.getBaggageWeight() - AppConstants.FREE_BAGGAGE_ALLOWANCE_KG) * AppConstants.EXCESS_BAGGAGE_FEE_PER_KG;
            }
            
            // Meal upgrade
            if (bp.hasMealUpgrade()) {
                passengerFare += AppConstants.MEAL_UPGRADE_FEE;
            }
            
            // Seat Selection: Premium for A, C, D, F
            if (bp.getSeatNumber() != null && !bp.getSeatNumber().isEmpty()) {
                char seatLetter = bp.getSeatNumber().charAt(bp.getSeatNumber().length() - 1);
                if (seatLetter == 'A' || seatLetter == 'F' || seatLetter == 'C' || seatLetter == 'D') {
                    passengerFare += AppConstants.PREMIUM_SEAT_FEE;
                }
            }
            
            totalPassengerFare += passengerFare;
        }

        double finalAmount = totalPassengerFare;
        
        // Surcharges per booking
        finalAmount += AppConstants.AIRPORT_CHARGES;
        finalAmount += AppConstants.FUEL_SURCHARGE;

        if (isExpress) {
            booking.setPriority(BookingPriority.EXPRESS);
            finalAmount += AppConstants.EXPRESS_BOOKING_FEE;
        } else {
            booking.setPriority(BookingPriority.REGULAR);
        }
        
        // GST for domestic flights (5%)
        if (isDomestic) {
            finalAmount += finalAmount * AppConstants.GST_RATE;
        }
        
        if (promoCode != null && promoCode.equalsIgnoreCase(AppConstants.PROMO_CODE_SKYBOOKER20)) {
            finalAmount = finalAmount * (1.0 - AppConstants.PROMO_DISCOUNT_RATE);
        }
        
        return finalAmount;
    }

    /**
     * Constructs a detailed textual invoice outlining all applied charges, discounts, and taxes per passenger.
     * 
     * @param booking    The reservation context containing passenger details
     * @param baseFare   The standard ticket price per seat
     * @param isExpress  Flag indicating expedited service inclusion
     * @param promoCode  The applied promotional coupon, if any
     * @param isDomestic Flag indicating if domestic taxes apply
     * @return A formatted string detailing the exact financial breakdown
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
                passengerFare *= AppConstants.CHILD_DISCOUNT_MULTIPLIER;
                sb.append(String.format("  Child Discount (25%%): -INR %.2f%n", baseFare * (1 - AppConstants.CHILD_DISCOUNT_MULTIPLIER)));
            } else if (bp.getAgeCategory().equalsIgnoreCase("Infant")) {
                passengerFare *= AppConstants.INFANT_DISCOUNT_MULTIPLIER;
                sb.append(String.format("  Infant Discount (90%%): -INR %.2f%n", baseFare * (1 - AppConstants.INFANT_DISCOUNT_MULTIPLIER)));
            }
            
            if (bp.getBaggageWeight() > AppConstants.FREE_BAGGAGE_ALLOWANCE_KG) {
                double excess = (bp.getBaggageWeight() - AppConstants.FREE_BAGGAGE_ALLOWANCE_KG) * AppConstants.EXCESS_BAGGAGE_FEE_PER_KG;
                passengerFare += excess;
                sb.append(String.format("  Excess Baggage (%.1f kg): +INR %.2f%n", (bp.getBaggageWeight() - AppConstants.FREE_BAGGAGE_ALLOWANCE_KG), excess));
            }
            
            if (bp.hasMealUpgrade()) {
                passengerFare += AppConstants.MEAL_UPGRADE_FEE;
                sb.append(String.format("  Meal Upgrade: +INR %.2f%n", AppConstants.MEAL_UPGRADE_FEE));
            }
            
            if (bp.getSeatNumber() != null && !bp.getSeatNumber().isEmpty()) {
                char seatLetter = bp.getSeatNumber().charAt(bp.getSeatNumber().length() - 1);
                if (seatLetter == 'A' || seatLetter == 'F' || seatLetter == 'C' || seatLetter == 'D') {
                    passengerFare += AppConstants.PREMIUM_SEAT_FEE;
                    sb.append(String.format("  Premium Seat (%s): +INR %.2f%n", bp.getSeatNumber(), AppConstants.PREMIUM_SEAT_FEE));
                }
            }
            sb.append(String.format("  Subtotal: INR %.2f%n", passengerFare));
            totalPassengerFare += passengerFare;
        }

        sb.append("--------------------------------------------\n");
        sb.append(String.format("Passengers Subtotal: INR %.2f%n", totalPassengerFare));
        
        double finalAmount = totalPassengerFare;
        
        sb.append(String.format("Airport Charges: +INR %.2f%n", AppConstants.AIRPORT_CHARGES));
        finalAmount += AppConstants.AIRPORT_CHARGES;
        
        sb.append(String.format("Fuel Surcharge: +INR %.2f%n", AppConstants.FUEL_SURCHARGE));
        finalAmount += AppConstants.FUEL_SURCHARGE;

        if (isExpress) {
            sb.append(String.format("Express Booking Fee: +INR %.2f%n", AppConstants.EXPRESS_BOOKING_FEE));
            finalAmount += AppConstants.EXPRESS_BOOKING_FEE;
        }
        
        if (isDomestic) {
            double gst = finalAmount * AppConstants.GST_RATE;
            sb.append(String.format("GST (%.0f%%): +INR %.2f%n", AppConstants.GST_RATE * 100, gst));
            finalAmount += gst;
        }
        
        if (promoCode != null && promoCode.equalsIgnoreCase(AppConstants.PROMO_CODE_SKYBOOKER20)) {
            double discount = finalAmount * AppConstants.PROMO_DISCOUNT_RATE;
            sb.append(String.format("Promo Discount (%.0f%%): -INR %.2f%n", AppConstants.PROMO_DISCOUNT_RATE * 100, discount));
            finalAmount -= discount;
        }
        
        sb.append("============================================");
        return sb.toString();
    }

    /**
     * Determines the eligible refund value and applies standard cancellation penalties based on the initial transaction amount.
     * 
     * @param totalFarePaid The originally settled transaction amount
     * @return A double array where index 0 contains the final refundable value and index 1 contains the deducted penalty
     */
    public double[] calculateRefundAndPenalty(double totalFarePaid) {
        double penalty = totalFarePaid * AppConstants.CANCELLATION_PENALTY_RATE;
        double refundAmount = totalFarePaid - penalty;
        return new double[]{refundAmount, penalty};
    }
}
