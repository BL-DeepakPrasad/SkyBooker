package com.airline.skybooker.models;

public class AirlineStaff extends User {
    public AirlineStaff(int userId, String fullName, String email) {
        super(userId, fullName, email, "STAFF");
    }
    
    @Override
    public void displayDashboard() { 
        System.out.println("--- Airline Staff Dashboard ---"); 
    }
}
