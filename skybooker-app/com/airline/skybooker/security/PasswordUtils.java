package com.airline.skybooker.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for handling password hashing and security operations.
 */
public class PasswordUtils {

    /**
     * Hashes a plain text password using SHA-256 and encodes it in Base64.
     *
     * @param plainPassword the raw password to hash
     * @return the hashed password string
     */
    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Verifies if a plain text password matches a given hash.
     *
     * @param plainPassword the raw password input during login
     * @param hashedPassword the stored hash from the database/memory
     * @return true if they match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        String newHash = hashPassword(plainPassword);
        return newHash.equals(hashedPassword);
    }
}
