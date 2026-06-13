package com.airline.skybooker.constants;

/**
 * Centralized repository for application-wide magic strings and numbers.
 * Enforces maintainability by avoiding hardcoded literals scattered across the codebase.
 */
public final class AppConstants {
    
    private AppConstants() {
    }

    // --- Booking Identifiers ---
    public static final String BOOKING_PREFIX = "BKG-";
    public static final String PNR_PREFIX = "PNR";

    // --- Status Flags ---
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_INITIATED = "INITIATED";

    // --- Pricing & Surcharges ---
    public static final double EXPRESS_BOOKING_FEE = 25.0;
    public static final double MEAL_UPGRADE_FEE = 20.0;
    public static final double PREMIUM_SEAT_FEE = 15.0;
    public static final double AIRPORT_CHARGES = 20.0;
    public static final double FUEL_SURCHARGE = 15.0;
    
    // --- Discounts & Taxation ---
    public static final double CHILD_DISCOUNT_MULTIPLIER = 0.75;
    public static final double INFANT_DISCOUNT_MULTIPLIER = 0.10;
    public static final double GST_RATE = 0.05;

    // --- Baggage Rules ---
    public static final double FREE_BAGGAGE_ALLOWANCE_KG = 15.0;
    public static final double EXCESS_BAGGAGE_FEE_PER_KG = 10.0;

    // --- Cancellation Policies ---
    public static final double CANCELLATION_PENALTY_RATE = 0.20;

    // --- Promotional Codes ---
    public static final String PROMO_CODE_SKYBOOKER20 = "SKYBOOKER20";
    public static final double PROMO_DISCOUNT_RATE = 0.20;

    // --- Notification Templates ---
    public static final String MSG_BOOKING_CONFIRMATION = "Your booking is confirmed! PNR: %s. Flight: %s from %s to %s.";
    public static final String MSG_E_TICKET = "Here is your E-Ticket details...\nPNR: %s\nTotal Fare: INR %.2f";
    public static final String MSG_FLIGHT_ALERT = "URGENT: Flight %s is %s. %s";
    public static final String MSG_TRAVEL_REMINDER = "REMINDER: Your flight (PNR: %s) %s";
    public static final String MSG_REFUND_INITIATED = "Your refund of INR %.2f for booking %s has been initiated.";
    public static final String MSG_REFUND_COMPLETED = "Your refund of INR %.2f has been successfully processed.";
    public static final String MSG_BOARDING_PASS = "Your boarding pass is ready:\n\n%s";
}
