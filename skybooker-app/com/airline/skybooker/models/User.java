package com.airline.skybooker.models;

import java.time.LocalDateTime;

/**
 * Abstract base class for all system users.
 * Demonstrates OOAD Inheritance and Abstraction.
 */
public abstract class User {
    protected int userId;
    protected String fullName;
    protected String email;
    protected String passwordHash;
    protected String phone;
    protected String role;
    protected boolean isActive;
    protected LocalDateTime createdAt;

    public User(int userId, String fullName, String email, String passwordHash, String phone, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public int getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }

    /**
     * Polymorphic method to be overridden by subclasses.
     */
    public abstract void displayDashboard();
}
