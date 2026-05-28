package com.airline.skybooker.exception;

public class PaymentFailureException extends RuntimeException {
    public PaymentFailureException(String msg) { super(msg); }
}
