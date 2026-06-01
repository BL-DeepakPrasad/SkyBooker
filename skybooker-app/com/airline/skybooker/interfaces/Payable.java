package com.airline.skybooker.interfaces;

/**
 * Interface defining operations for objects that can process payments.
 */
public interface Payable {
    /**
     * Processes a payment of the specified amount.
     * @param amount The amount to pay
     * @return true if successful
     */
    boolean processPayment(double amount);

    /**
     * Refunds a specified amount.
     * @param amount The amount to refund
     * @return true if successful
     */
    boolean refund(double amount);

    /**
     * Validates the payment details.
     * @return true if valid
     */
    boolean validate();
}
