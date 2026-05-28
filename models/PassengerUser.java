package com.airline.skybooker.models;

public class PassengerUser extends User {
    public PassengerUser(int userId, String fullName, String email) {
        super(userId, fullName, email, "PASSENGER");
    }
    @Override
    public void displayDashboard() { System.out.println("--- Passenger Dashboard ---"); }
}

