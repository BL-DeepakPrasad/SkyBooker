package com.airline.skybooker.models;


public class AdminUser extends User {
    public AdminUser(int userId, String fullName, String email) {
        super(userId, fullName, email, "ADMIN");
    }
    @Override
    public void displayDashboard() { System.out.println("--- Admin System Dashboard ---"); }
}