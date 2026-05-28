package com.airline.skybooker.models;

import com.airline.skybooker.interfaces.Payable;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Payment implements Payable {
    protected String transactionId;
    protected LocalDateTime timestamp;
    protected double amount;

    public Payment(double amount) {
        this.transactionId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.amount = amount;
    }

    public String getTransactionId() { return transactionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getAmount() { return amount; }
}
