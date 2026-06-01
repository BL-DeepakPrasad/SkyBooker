package com.airline.skybooker.payments;

public class EMIPayment implements PaymentStrategy {
    
    private final String cardNumber;
    private final int months;

    public EMIPayment(String cardNumber, int months) {
        this.cardNumber = cardNumber;
        this.months = months;
    }

    @Override
    public boolean validate() {
        if (cardNumber == null || cardNumber.replaceAll("\\s+", "").length() != 16) {
            System.out.println("[PAYMENT GATEWAY] Error: Invalid card for EMI.");
            return false;
        }
        if (months != 3 && months != 6 && months != 12) {
            System.out.println("[PAYMENT GATEWAY] Error: Invalid EMI tenure.");
            return false;
        }
        return true;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("\n[PAYMENT GATEWAY] Connecting to EMI network...");
        
        if (!validate()) {
            return false;
        }
        
        double monthlyInstallment = amount / months;
        System.out.printf("[PAYMENT GATEWAY] Processing EMI of INR %.2f per month for %d months.%n", monthlyInstallment, months);
        System.out.println("[PAYMENT GATEWAY] Transaction Approved.");
        return true;
    }

    @Override
    public boolean refund(double amount) {
        System.out.println("[PAYMENT GATEWAY] Initiating EMI cancellation for Card ending in " + cardNumber.substring(cardNumber.length() - 4));
        System.out.println("[PAYMENT GATEWAY] EMI Foreclosure and Refund Processed Successfully.");
        return true;
    }
}
