package com.airline.skybooker.managers;

import com.airline.skybooker.interfaces.Payable;
import com.airline.skybooker.exception.NetworkTimeoutException;
import com.airline.skybooker.exception.PaymentFailureException;

/**
 * Gateway orchestrator handling financial transactions and refund routing.
 * Ensures stateful tracking of successful and failed payment operations.
 */
public class PaymentManager {

    private static volatile PaymentManager instance;
    private int successfulTransactions = 0;
    private int failedTransactions = 0;

    private PaymentManager() {}

    /**
     * Retrieves the singleton instance of the PaymentManager.
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
     * Executes a payment transaction using the provided payment strategy.
     * Includes simulated network latency and failure scenarios.
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
        
        // Simulate Network Timeout (5% chance)
        if (Math.random() < 0.05) {
            throw new NetworkTimeoutException("Payment Gateway Timeout. Please try again.");
        }
        
        // Simulate Hard Payment Failure (5% chance)
        if (Math.random() < 0.05) {
            failedTransactions++;
            throw new PaymentFailureException("Transaction declined by bank.");
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
     * Re-routes a refund transaction through the original payment strategy utilized during booking.
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
    
    public int getSuccessfulTransactions() { return successfulTransactions; }
    public int getFailedTransactions() { return failedTransactions; }
}
