package com.airline.skybooker.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Static validation engine asserting structural and domain compliance for raw inputs.
 * Employs fail-fast mechanisms throwing IllegalArgumentException upon detecting malformed data.
 */
public class ValidationUtils {

    /**
     * Enforces the International Air Transport Association (IATA) 3-letter code standard.
     * 
     * @param code The geographic identifier to inspect
     * @throws IllegalArgumentException if the sequence violates length or character constraints
     */
    public static void validateAirportCode(String code) {
        if (code == null || !code.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Invalid airport code format. Must be exactly 3 uppercase letters (e.g., DEL, BOM).");
        }
    }

    /**
     * Ensures group reservations remain within permissible operational capacity limits.
     * 
     * @param count The desired number of simultaneous ticket allocations
     * @throws IllegalArgumentException if the aggregate size exceeds physical or business constraints
     */
    public static void validatePassengerCount(int count) {
        if (count < 1 || count > 6) {
            throw new IllegalArgumentException("Invalid passenger count. Must be between 1 and 6.");
        }
    }

    /**
     * Verifies string representations align with expected localized calendar structures (dd/MM/yyyy).
     * 
     * @param dateStr The temporal string payload to verify
     * @throws IllegalArgumentException if the text cannot map to a valid Gregorian date
     */
    public static void validateDateFormat(String dateStr) {
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use dd/MM/yyyy.");
        }
    }

    /**
     * Asserts primary account number formatting strictly adheres to a 16-digit structure.
     * 
     * @param card The raw billing number to evaluate
     * @throws IllegalArgumentException if the data contains non-numeric or length anomalies
     */
    public static void validateCardNumber(String card) {
        if (card == null || !card.matches("^\\d{16}$")) {
            throw new IllegalArgumentException("Invalid card number. Must be exactly 16 digits.");
        }
    }

    /**
     * Inspects the card security code to verify standard 3-digit verification limits.
     * 
     * @param cvv The raw security pin string
     * @throws IllegalArgumentException if the token is absent or malformed
     */
    public static void validateCvv(String cvv) {
        if (cvv == null || !cvv.matches("^\\d{3}$")) {
            throw new IllegalArgumentException("Invalid CVV. Must be exactly 3 digits.");
        }
    }

    /**
     * Validates Virtual Payment Address (VPA) structures against core UPI network requirements.
     * 
     * @param upi The raw payment routing address
     * @throws IllegalArgumentException if the handle lacks the required domain delimiter
     */
    public static void validateUpiId(String upi) {
        if (upi == null || !upi.matches("^[\\w.-]+@[\\w.-]+$")) {
            throw new IllegalArgumentException("Invalid UPI ID. Expected format: name@bank.");
        }
    }

    /**
     * Applies standard regular expression rules to ensure logical email transmission boundaries.
     * 
     * @param email The target correspondence address
     * @throws IllegalArgumentException if the sequence violates basic RFC 5322 structure
     */
    public static void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    /**
     * Confirms the telephonic sequence contains exactly 10 digits for local route dialing.
     * 
     * @param phone The string representing the subscriber number
     * @throws IllegalArgumentException if country codes or incorrect lengths are detected
     */
    public static void validatePhone(String phone) {
        if (phone == null || !phone.matches("^\\d{10}$")) {
            throw new IllegalArgumentException("Invalid phone number. Must be exactly 10 digits.");
        }
    }

    /**
     * Enforces minimum character constraints on passenger nomenclature to prevent anonymous entries.
     * 
     * @param name The provided legal or preferred identifier
     * @throws IllegalArgumentException if the identifier is effectively empty or single-character
     */
    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty() || name.length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters long.");
        }
    }

    /**
     * Validates credential density requirements to maintain minimal cryptographic security.
     * 
     * @param password The raw authentication secret
     * @throws IllegalArgumentException if the secret is beneath acceptable entropy limits
     */
    public static void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
    }

    /**
     * Asserts international travel document structures conform to alphanumeric length regulations.
     * 
     * @param passport The physical document serial identifier
     * @throws IllegalArgumentException if irregular characters or invalid lengths exist
     */
    public static void validatePassport(String passport) {
        if (passport == null || !passport.matches("^[A-Za-z0-9]{6,15}$")) {
            throw new IllegalArgumentException("Invalid passport format. Must be 6-15 alphanumeric characters.");
        }
    }
}
