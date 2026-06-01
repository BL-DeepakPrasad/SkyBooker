package com.airline.skybooker.payments;

/**
 * The Strategy Interface for processing payments.
 * Allows the PaymentManager to process different types of payments
 * without knowing their implementation details.
 */
public interface PaymentStrategy {
    
    /**
     * Processes a payment of the specified amount.
     * 
     * @param amount the amount to charge
     * @return true if successful, false otherwise
     */
    boolean processPayment(double amount);

    /**
     * Validates the payment details.
     * 
     * @return true if valid, false otherwise
     */
    boolean validate();

    /**
     * Processes a refund of the specified amount.
     * 
     * @param amount the amount to refund
     * @return true if successful, false otherwise
     */
    boolean refund(double amount);
}
