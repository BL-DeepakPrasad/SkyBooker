package com.airline.skybooker.models;

import java.time.LocalDateTime;
import com.airline.skybooker.enums.Role;

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
    protected Role role;
    protected boolean isActive;
    protected LocalDateTime createdAt;

    public User(int userId, String fullName, String email, String passwordHash, String phone, Role role) {
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
    public Role getRole() { return role; }
    public String getPhone() { return phone; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Polymorphic method to be overridden by subclasses.
     */
    public abstract void displayDashboard();
}
