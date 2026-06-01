package com.airline.skybooker.exception;

/**
 * Represents a declined or failed financial transaction during the checkout pipeline.
 * Wraps integration errors from payment gateways or inadequate fund responses.
 */
public class PaymentFailureException extends AirlineSystemException {
    /**
     * Records a specific transactional failure point.
     * 
     * @param message The exact rejection reason from the payment gateway
     */
    public PaymentFailureException(String message) {
        super(message);
    }
}
