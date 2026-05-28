package com.airline.skybooker.models;
import com.airline.skybooker.security.EncryptionUtil;

public class CardPayment extends Payment {
    private String encryptedCardNumber;

    public CardPayment(double amount, String cardNumber) {
        super(amount);
        this.encryptedCardNumber = EncryptionUtil.encrypt(cardNumber);
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("Processing Card Payment for amount $" + amount);
        System.out.println("Securely accessing card: " + EncryptionUtil.decrypt(encryptedCardNumber).replaceAll(".(?=.{4})", "*"));
        return true;
    }
}
