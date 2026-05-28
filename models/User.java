package com.airline.skybooker.models;

public abstract class User {
    private int userId;

    public String getEmail() {
        return email;
    }

    private String fullName;
    private String email;
    private String role;

    public User(int userId, String fullName, String email, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }

    public abstract void displayDashboard();
}