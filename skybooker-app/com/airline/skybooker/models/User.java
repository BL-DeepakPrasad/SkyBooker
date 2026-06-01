package com.airline.skybooker.models;

import java.time.LocalDateTime;
import com.airline.skybooker.enums.Role;

/**
 * Foundational abstraction for all authenticated entities interacting with the platform.
 * Centralizes common identity attributes, contact information, and role-based access.
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

    /**
     * Bootstraps common profile attributes for a newly authenticated entity.
     * Records the creation timestamp and activates the profile.
     *
     * @param userId       unique system identifier for the entity
     * @param fullName     complete legal name
     * @param email        primary contact and login email address
     * @param passwordHash secured hash for authentication checks
     * @param phone        primary contact phone number
     * @param role         authorization level defining system permissions
     */
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
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    /**
     * Renders role-specific interfaces and operational menus.
     * Must be implemented by concrete subclasses to provide tailored dashboard views.
     */
    public abstract void displayDashboard();
}
