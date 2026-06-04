package com.airline.skybooker.managers;

import com.airline.skybooker.interfaces.Payable;
import com.airline.skybooker.exception.NetworkTimeoutException;
import com.airline.skybooker.exception.PaymentFailureException;

/**
 * Handles all money coming in and going out of the system.
 * Connects to different payment methods (like Credit Cards or UPI) to process ticket purchases and issue refunds for canceled flights.
 */
public class PaymentManager {

    private static volatile PaymentManager instance;
    private int successfulTransactions = 0;
    private int failedTransactions = 0;

    private PaymentManager() {}

    /**
     * Provides access to the single, shared PaymentManager instance.
     * Keeps a running tally of successful and failed transactions across the entire app.
     *
     * @return the singleton PaymentManager instance
     */
    public static PaymentManager getInstance() {
        if (instance == null) {
            synchronized (PaymentManager.class) {
                if (instance == null) {
                    instance = new PaymentManager();
                }
            }
        }
        return instance;
    }

    /**
     * Attempts to charge the customer's chosen payment method for their flight.
     * Simulates real-world problems like bad internet connections or declined cards so the app can practice handling errors.
     *
     * @param strategy the payment method abstraction (e.g., Credit Card, UPI)
     * @param amount   the total fiat value to be processed
     * @return true if the transaction was processed successfully
     * @throws NetworkTimeoutException if the simulated gateway times out
     * @throws PaymentFailureException if the simulated transaction is explicitly declined
     */
    public boolean processTransaction(Payable strategy, double amount) throws NetworkTimeoutException, PaymentFailureException {
        if (strategy == null) {
            throw new IllegalArgumentException("Payment strategy cannot be null");
        }

        System.out.println("[PAYMENT GATEWAY] Processing transaction of INR " + amount + "...");
        boolean success = strategy.processPayment(amount);
        if (success) {
            successfulTransactions++;
        } else {
            failedTransactions++;
        }
        return success;
    }

    /**
     * Sends money back to the customer using the exact same method they used to pay.
     * Used when a customer cancels their booking or if the airline cancels the flight.
     *
     * @param strategy the original payment method utilized
     * @param amount   the value to be refunded to the customer
     * @return true if the refund was successfully processed, false otherwise
     */
    public boolean processRefund(Payable strategy, double amount) {
        if (strategy == null) {
            System.out.println("[PAYMENT GATEWAY] ERROR: Original payment strategy not found for refund.");
            failedTransactions++;
            return false;
        }
        System.out.println("[PAYMENT GATEWAY] Processing refund of INR " + amount + "...");
        boolean success = strategy.refund(amount);
        if (success) {
            successfulTransactions++;
        } else {
            failedTransactions++;
        }
        return success;
    }
    
    /**
     * Returns the total number of successful payments and refunds processed.
     * @return successful transaction count
     */
    public int getSuccessfulTransactions() { return successfulTransactions; }

    /**
     * Returns the total number of failed payment attempts (e.g., declined cards or timeouts).
     * @return failed transaction count
     */
    public int getFailedTransactions() { return failedTransactions; }
}
