package com.airline.skybooker.payments;

/**
 * Financial processor handling Unified Payments Interface (UPI) transfers.
 * Dispatches payment requests to virtual payment addresses and awaits asynchronous approval.
 */
public class UPIPayment extends AbstractPayment {

    private final String upiId;

    /**
     * Constructs a UPI transaction request bound to a specific virtual address.
     * 
     * @param upiId The registered Virtual Payment Address (VPA) of the payer
     */
    public UPIPayment(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public boolean validate() {
        if (upiId == null || !upiId.contains("@")) {
            System.out.println("[PAYMENT GATEWAY] Error: Invalid UPI ID format. Must contain '@'.");
            return false;
        }
        return true;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("\n[PAYMENT GATEWAY] Connecting to UPI network...");
        
        if (!validate()) {
            return false;
        }
        
        System.out.println("[PAYMENT GATEWAY] Sending payment request to " + upiId + " for INR " + amount);
        System.out.println("[UPI APP] Please approve the request in your UPI App...");
        System.out.println("[PAYMENT GATEWAY] Transaction Approved.");
        return true;
    }

    @Override
    public boolean refund(double amount) {
        System.out.println("[PAYMENT GATEWAY] Initiating refund of INR " + amount + " to UPI ID: " + upiId);
        System.out.println("[PAYMENT GATEWAY] Refund Processed Successfully.");
        return true;
    }
}
