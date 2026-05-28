package com.airline.skybooker.models;

public class UPIPayment extends Payment {
    private String upiId;

    public UPIPayment(double amount, String upiId) {
        super(amount);
        this.upiId = upiId;
    }

    @Override
    public boolean processPayment(double amount) {
        System.out.println("Processing UPI Payment for amount $" + amount + " to ID " + upiId);
        return true;
    }
}
