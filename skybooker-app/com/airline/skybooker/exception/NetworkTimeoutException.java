package com.airline.skybooker.exception;

public class NetworkTimeoutException extends AirlineSystemException {
    public NetworkTimeoutException(String message) {
        super(message);
    }
}
