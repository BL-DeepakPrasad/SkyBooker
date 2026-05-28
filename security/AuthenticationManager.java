package com.airline.skybooker.security;

import com.airline.skybooker.exception.AirlineSystemException;
import com.airline.skybooker.models.AdminUser;
import com.airline.skybooker.models.PassengerUser;
import com.airline.skybooker.models.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AuthenticationManager {
    private static volatile AuthenticationManager instance;

    // Simulating a user database
    private Map<String, User> userDatabase;
    private Map<String, String> userPasswords;


    private AuthenticationManager() {
        this.userDatabase = new HashMap<>();
        this.userPasswords = new HashMap<>();


        User admin = new AdminUser(1, "deepak prasad", "admin@airline.com");
        User passenger = new PassengerUser(2, "raj", "raj@example.com");

        userDatabase.put(admin.getEmail(), admin);
        userPasswords.put(admin.getEmail(), PasswordEncoder.encode("admin123"));

        userDatabase.put(passenger.getEmail(), passenger);
        userPasswords.put(passenger.getEmail(), PasswordEncoder.encode("pass123"));
    }

    public static AuthenticationManager getInstance() {
        if (instance == null) {
            synchronized (AuthenticationManager.class) {
                if (instance == null) {
                    instance = new AuthenticationManager();
                }
            }
        }
        return instance;
    }


    public boolean authenticate(String email, String rawPassword) {
        if (!userDatabase.containsKey(email)) {
            throw new AirlineSystemException("User not found.");
        }

        String storedHash = userPasswords.get(email);
        if (PasswordEncoder.matches(rawPassword, storedHash)) {
            System.out.println("Credentials verified. Proceeding to ..MFA ");
            return true;
        }
        System.out.println("Invalid credentials.");
        return false;
    }

    // Generate and Verify OTP
    public String generateOTP(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        System.out.println("[SYSTEM MOCK] OTP sent to " + email + ": " + otp);
        return otp;
    }

    public User verifyOTPAndLogin(String email, String inputOtp, String actualOtp) {
        if (inputOtp.equals(actualOtp)) {
            User user = userDatabase.get(email);
            System.out.println("Login successful. Welcome, " + user.getFullName());
            return user;
        }
        System.out.println("Invalid OTP.");
        return null;
    }

    public void logout() {
        System.out.println("Logged out successfully.");
    }
}