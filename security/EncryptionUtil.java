package com.airline.skybooker.security;
import java.util.Base64;

public class EncryptionUtil {

    public static String encrypt(String rawData) {
        return Base64.getEncoder().encodeToString(rawData.getBytes());
    }
    public static String decrypt(String encryptedData) {
        return new String(Base64.getDecoder().decode(encryptedData));
    }
}
