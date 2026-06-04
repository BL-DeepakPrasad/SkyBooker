package com.airline.skybooker.exception;

/**
 * Thrown when a user's payment is declined or fails for any reason.
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
