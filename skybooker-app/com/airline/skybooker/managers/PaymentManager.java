package com.airline.skybooker.managers;

import com.airline.skybooker.payments.PaymentStrategy;

/**
 * Singleton Manager responsible for executing payment strategies.
 * Satisfies Module 11.3 (PaymentManager Singleton).
 */
public class PaymentManager {

    private static volatile PaymentManager instance;

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
     * Executes the provided payment strategy.
     * 
     * @param strategy The concrete payment strategy (Card, UPI, etc.)
     * @param amount The amount to charge
     * @return true if payment succeeds
     */
    public boolean processTransaction(PaymentStrategy strategy, double amount) {
        if (strategy == null) {
            throw new IllegalArgumentException("Payment strategy cannot be null");
        }
        return strategy.processPayment(amount);
    }

    /**
     * Executes a refund through the provided payment strategy.
     * 
     * @param strategy The original payment strategy used
     * @param amount The amount to refund
     * @return true if refund succeeds
     */
    public boolean processRefund(PaymentStrategy strategy, double amount) {
        if (strategy == null) {
            System.out.println("[REFUND] Error: No original payment method found.");
            return false;
        }
        return strategy.refund(amount);
    }
}
