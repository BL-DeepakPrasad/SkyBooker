package com.airline.skybooker.payments;

public class UPIPayment implements PaymentStrategy {

    private final String upiId;

    public UPIPayment(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("\n[PAYMENT GATEWAY] Connecting to UPI network...");
        
        // Basic Mock Validation
        if (upiId == null || !upiId.contains("@")) {
            System.out.println("[PAYMENT GATEWAY] Error: Invalid UPI ID format. Must contain '@'.");
            return false;
        }
        
        System.out.println("[PAYMENT GATEWAY] Sending payment request to " + upiId + " for $" + amount);
        System.out.println("[PAYMENT GATEWAY] Transaction Approved.");
        return true;
    }
}
