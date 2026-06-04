package com.airline.skybooker.services;

import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.BookingPassenger;
import com.airline.skybooker.enums.BookingPriority;
import com.airline.skybooker.constants.AppConstants;

/**
 * Calculates the total cost of a booking.
 * It exists to keep all pricing rules, discounts, taxes, and fees in one place, 
 * separate from the actual booking and payment processes.
 */
public class FareCalculatorService {

    private static volatile FareCalculatorService instance;

    private FareCalculatorService() {}

    /**
     * Gets the single, shared instance of this service.
     * This ensures everyone in the system gets exactly the same fare calculations.
     *
     * @return the FareCalculatorService instance
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
     * Calculates how much the customer needs to pay.
     * It adds up the base price, seat upgrades, taxes, and applies any discounts.
     * 
     * @param booking    the booking containing passengers
     * @param baseFare   the starting price of the ticket
     * @param isExpress  whether the user chose faster processing
     * @param promoCode  a discount code, if any
    public double calculateFinalFare(Booking booking, double baseFare, boolean isExpress, String promoCode) {
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
        
        // Apply GST
        finalAmount += finalAmount * AppConstants.GST_RATE;
        
        if (promoCode != null && promoCode.equalsIgnoreCase(AppConstants.PROMO_CODE_SKYBOOKER20)) {
            finalAmount = finalAmount * (1.0 - AppConstants.PROMO_DISCOUNT_RATE);
        }
        
        return finalAmount;
    }

    /**
     * Creates a text receipt showing a detailed breakdown of all charges.
     * This helps the customer understand exactly what they are paying for.
     * 
     * @param booking    the booking containing passengers
     * @param baseFare   the starting price of the ticket
     * @param isExpress  whether the user chose faster processing
     * @param promoCode  a discount code, if any
    public String getFareBreakdown(Booking booking, double baseFare, boolean isExpress, String promoCode) {
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
        
        double gst = finalAmount * AppConstants.GST_RATE;
        sb.append(String.format("GST (%.0f%%): +INR %.2f%n", AppConstants.GST_RATE * 100, gst));
        finalAmount += gst;
        
        if (promoCode != null && promoCode.equalsIgnoreCase(AppConstants.PROMO_CODE_SKYBOOKER20)) {
            double discount = finalAmount * AppConstants.PROMO_DISCOUNT_RATE;
            sb.append(String.format("Promo Discount (%.0f%%): -INR %.2f%n", AppConstants.PROMO_DISCOUNT_RATE * 100, discount));
            finalAmount -= discount;
        }
        
        sb.append("============================================");
        return sb.toString();
    }

    /**
     * Figures out how much money to give back and how much to keep as a fee when a ticket is cancelled.
     * This enforces the airline's cancellation policy.
     * 
     * @param totalFarePaid the total amount the customer originally paid
     * @return an array with two values: [amount to refund, amount kept as penalty]
     */
    public double[] calculateRefundAndPenalty(double totalFarePaid) {
        double penalty = totalFarePaid * AppConstants.CANCELLATION_PENALTY_RATE;
        double refundAmount = totalFarePaid - penalty;
        return new double[]{refundAmount, penalty};
    }
}
