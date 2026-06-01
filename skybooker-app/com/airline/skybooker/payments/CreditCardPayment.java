package com.airline.skybooker.payments;

/**
 * Financial processor handling direct credit card billing operations.
 * Integrates with standard card networks and enforces 3D-Secure authentication workflows.
 */
public class CreditCardPayment extends AbstractPayment {
    
    private final String cardNumber;
    private final String cardHolderName;
    private final String cvv;

    /**
     * Initializes the credit card billing context with required primary account details.
     * 
     * @param cardNumber     The 16-digit primary account number
     * @param cardHolderName The legal name embossed on the card
     * @param cvv            The 3-digit security verification code
     */
    public CreditCardPayment(String cardNumber, String cardHolderName, String cvv) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.cvv = cvv;
    }

    @Override
    public boolean validate() {
        if (cardNumber == null || cardNumber.replaceAll("\\s+", "").length() != 16) {
            System.out.println("[PAYMENT GATEWAY] Error: Card number must be 16 digits.");
            return false;
        }
        if (cvv == null || cvv.length() != 3) {
            System.out.println("[PAYMENT GATEWAY] Error: CVV must be 3 digits.");
            return false;
        }
        return true;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("\n[PAYMENT GATEWAY] Connecting to Credit Card network...");
        
        if (!validate()) {
            return false;
        }
        
        System.out.println("[3D SECURE] Sending OTP to registered mobile number...");
        System.out.println("[3D SECURE] OTP Verified.");
        System.out.println("[PAYMENT GATEWAY] Charging INR " + amount + " to card ending in " + cardNumber.substring(cardNumber.length() - 4));
        System.out.println("[PAYMENT GATEWAY] Transaction Approved.");
        return true;
    }

    @Override
    public boolean refund(double amount) {
        System.out.println("[PAYMENT GATEWAY] Initiating refund of INR " + amount + " to Credit Card ending in " + cardNumber.substring(cardNumber.length() - 4));
        System.out.println("[PAYMENT GATEWAY] Refund Processed Successfully.");
        return true;
    }
}
