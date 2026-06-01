package com.airline.skybooker.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidationUtils {

    public static void validateAirportCode(String code) {
        if (code == null || !code.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Invalid airport code format. Must be exactly 3 uppercase letters (e.g., DEL, BOM).");
        }
    }

    public static void validatePassengerCount(int count) {
        if (count < 1 || count > 6) {
            throw new IllegalArgumentException("Invalid passenger count. Must be between 1 and 6.");
        }
    }

    public static void validateDateFormat(String dateStr) {
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use dd/MM/yyyy.");
        }
    }

    public static void validateCardNumber(String card) {
        if (card == null || !card.matches("^\\d{16}$")) {
            throw new IllegalArgumentException("Invalid card number. Must be exactly 16 digits.");
        }
    }

    public static void validateCvv(String cvv) {
        if (cvv == null || !cvv.matches("^\\d{3}$")) {
            throw new IllegalArgumentException("Invalid CVV. Must be exactly 3 digits.");
        }
    }

    public static void validateUpiId(String upi) {
        if (upi == null || !upi.matches("^[\\w.-]+@[\\w.-]+$")) {
            throw new IllegalArgumentException("Invalid UPI ID. Expected format: name@bank.");
        }
    }

    public static void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    public static void validatePhone(String phone) {
        if (phone == null || !phone.matches("^\\d{10}$")) {
            throw new IllegalArgumentException("Invalid phone number. Must be exactly 10 digits.");
        }
    }

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty() || name.length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters long.");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
    }

    public static void validatePassport(String passport) {
        if (passport == null || !passport.matches("^[A-Z0-9]{6,9}$")) {
            throw new IllegalArgumentException("Invalid passport format. Must be 6-9 alphanumeric characters.");
        }
    }
}
