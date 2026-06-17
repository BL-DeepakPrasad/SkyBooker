package com.airline.skybooker.models;

import java.time.LocalDateTime;
import com.airline.skybooker.enums.Role;

/**
 * Base profile for anyone who logs into the system (passengers, admins, airline staff).
 * We need this class so we don't have to rewrite the code for basic things like passwords, emails, and names in every single user type.
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
     * Sets up the common account details that every person needs to log in.
     * It automatically marks the account as active and records the exact time it was created.
     *
     * @param userId       unique ID number in the database
     * @param fullName     the person's real name
     * @param email        email address used for logging in
     * @param passwordHash scrambled password for security
     * @param phone        contact phone number
     * @param role         what type of user this is (determines what they are allowed to do)
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
     * Shows a customized menu based on who is logged in.
     * Because this is abstract, every specific user type (like Passenger or Admin) must provide their own version of this menu.
     */
    public abstract void displayDashboard();
}
