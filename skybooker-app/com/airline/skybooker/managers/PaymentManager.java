package com.airline.skybooker.managers;

import com.airline.skybooker.payments.PaymentStrategy;
import com.airline.skybooker.exception.NetworkTimeoutException;
import com.airline.skybooker.exception.PaymentFailureException;

/**
 * Singleton managing payment processing and refunds.
 */
public class PaymentManager {

    private static volatile PaymentManager instance;
    private int successfulTransactions = 0;
    private int failedTransactions = 0;

    private PaymentManager() {}

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
     * Executes a payment through the provided strategy.
     */
    public boolean processTransaction(PaymentStrategy strategy, double amount) throws NetworkTimeoutException, PaymentFailureException {
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
     * Re-routes a refund through the original payment strategy.
     */
    public boolean processRefund(PaymentStrategy strategy, double amount) {
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
