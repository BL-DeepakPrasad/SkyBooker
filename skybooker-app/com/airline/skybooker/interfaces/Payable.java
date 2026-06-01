package com.airline.skybooker.interfaces;

/**
 * Financial operations contract for processing transactions and issuing refunds.
 */
public interface Payable {
    /**
     * Executes the financial transaction for the given charge.
     * 
     * @param amount The total cost to deduct
     * @return true if the transaction completes authorized, false otherwise
     */
    boolean processPayment(double amount);

    /**
     * Reverts a previously captured transaction and returns funds.
     * 
     * @param amount The specific monetary value to refund
     * @return true if the refund succeeds, false otherwise
     */
    boolean refund(double amount);

    /**
     * Verifies the integrity and authorization readiness of the payment credentials.
     * 
     * @return true if the credentials pass validation rules, false otherwise
     */
    boolean validate();
}
