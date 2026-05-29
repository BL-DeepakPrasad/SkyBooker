package com.airline.skybooker.payments;

public class CreditCardPayment implements PaymentStrategy {
    
    private final String cardNumber;
    private final String cardHolderName;
    private final String cvv;

    public CreditCardPayment(String cardNumber, String cardHolderName, String cvv) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cvv = cvv;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("\n[PAYMENT GATEWAY] Connecting to Credit Card network...");
        
        // Basic Mock Validation
        if (cardNumber == null || cardNumber.length() < 12) {
            System.out.println("[PAYMENT GATEWAY] Error: Invalid card number length.");
            return false;
        }
        
        System.out.println("[PAYMENT GATEWAY] Charging $" + amount + " to card ending in " + cardNumber.substring(cardNumber.length() - 4));
        System.out.println("[PAYMENT GATEWAY] Transaction Approved.");
        return true;
    }
}
