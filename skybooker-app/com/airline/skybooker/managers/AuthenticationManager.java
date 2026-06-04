package com.airline.skybooker.managers;

import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.models.Admin;
import com.airline.skybooker.models.AirlineStaff;
import com.airline.skybooker.security.PasswordUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages user accounts, logins, and role-based permissions (like Admin or Passenger).
 * Centralizes all security features to ensure users can only access their own data and features they are authorized for.
 */
public class AuthenticationManager {

    private static volatile AuthenticationManager instance;
    private final Map<Integer, User> userDatabase; // UserId -> User
    private final Map<String, Integer> emailToIdMap;
    private final AtomicInteger idGenerator;
    private User currentUser;

    private AuthenticationManager() {
        this.userDatabase = new ConcurrentHashMap<>();
        this.emailToIdMap = new ConcurrentHashMap<>();
        this.idGenerator = new AtomicInteger(1000);
        initializeMockAdmin();
        initializeMockPassenger();
        initializeMockStaff();
    }

    /**
     * Provides access to the single, shared AuthenticationManager instance.
     * Ensures the entire application checks against the same user database when authenticating actions.
     *
     * @return the singleton AuthenticationManager instance
     */
    public static AuthenticationManager getInstance() {
        if (instance == null) {
            synchronized (AuthenticationManager.class) {
                if (instance == null) {
                    instance = new AuthenticationManager();
                }
            }
        }
        return instance;
    }

    private void initializeMockAdmin() {
        registerAdmin("System Admin", "admin@skybooker.com", "admin123");
    }

    private void initializeMockPassenger() {
        registerPassenger("Test Passenger", "user@skybooker.com", "user123", "1234567890", "TEST1234", "Indian");
    }

    private void initializeMockStaff() {
        int id = idGenerator.incrementAndGet();
        String hash = PasswordUtils.hashPassword("staff123");
        AirlineStaff s = new AirlineStaff(id, "Test Staff", "staff@skybooker.com", hash, "1234567890");
        userDatabase.put(id, s);
        emailToIdMap.put("staff@skybooker.com", id);
    }

    /**
     * Creates a new customer account so they can book flights and view their travel history.
     * Checks if the email is already in use to prevent duplicate accounts.
     *
     * @param fullName       the passenger's full legal name
     * @param email          the passenger's contact email address, utilized for login
     * @param password       the raw password to be hashed and stored
     * @param phone          the passenger's contact phone number
     * @param passportNumber the international passport number
     * @param nationality    the passenger's country of citizenship
     * @return true if the registration was successful, false if the email already exists
     */
    public boolean registerPassenger(String fullName, String email, String password, String phone, String passportNumber, String nationality) {
        if (emailToIdMap.containsKey(email.toLowerCase())) {
            return false; // Email exists
        }
        int id = idGenerator.incrementAndGet();
        String hash = PasswordUtils.hashPassword(password);
        Passenger p = new Passenger(id, fullName, email.toLowerCase(), hash, phone, passportNumber, nationality);
        userDatabase.put(id, p);
        emailToIdMap.put(email.toLowerCase(), id);
        return true;
    }

    private void registerAdmin(String fullName, String email, String password) {
        int id = idGenerator.incrementAndGet();
        String hash = PasswordUtils.hashPassword(password);
        Admin a = new Admin(id, fullName, email.toLowerCase(), hash, "");
        userDatabase.put(id, a);
        emailToIdMap.put(email.toLowerCase(), id);
    }

    /**
     * Checks a user's email and password to securely log them into the system.
     * If successful, it remembers the user so they don't have to log in repeatedly during their session.
     *
     * @param email    the user's registered email address
     * @param password the user's raw password
     * @return true if credentials match and the account is active, false otherwise
     */
    public boolean login(String email, String password) {
        Integer id = emailToIdMap.get(email.toLowerCase());
        if (id != null) {
            User user = userDatabase.get(id);
            if (!user.isActive()) {
                System.out.println("Account is suspended. Please contact support.");
                return false;
            }
            if (PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                this.currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a specific user using their internal system ID number.
     * Used internally by other parts of the system, like when linking a booking to the user who made it.
     *
     * @param userId the unique identifier of the user
     * @return an Optional containing the matched User, or empty if not found
     */
    public Optional<User> getUserById(int userId) {
        return Optional.ofNullable(userDatabase.get(userId));
    }

    /**
     * Returns a list of everyone registered in the system, including admins and staff.
     * Used by administrators to view and manage user accounts.
     *
     * @return a list encompassing all User profiles within the system
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(userDatabase.values());
    }

    /**
     * Looks up a user account by their email address.
     * Helpful for features like password resets or checking if an email is already taken.
     *
     * @param email the user's email address
     * @return an Optional containing the corresponding User, or empty if no match exists
     */
    public Optional<User> getUserByEmail(String email) {
        Integer id = emailToIdMap.get(email.toLowerCase());
        return id != null ? Optional.ofNullable(userDatabase.get(id)) : Optional.empty();
    }

    /**
     * Suspends or reactivates a user's account.
     * Used by administrators to ban users who violate terms of service, preventing them from logging in.
     *
     * @param userId   the unique identifier of the user
     * @param isActive the desired active state (true for active, false for suspended)
     * @return true if the status was successfully toggled, false if the user was not found
     */
    public boolean toggleUserStatus(int userId, boolean isActive) {
        User user = userDatabase.get(userId);
        if (user != null) {
            user.setActive(isActive);
            return true;
        }
        return false;
    }

    /**
     * Upgrades a regular passenger to an employee role.
     * Used when the airline hires someone and needs to grant them staff privileges (like accessing the check-in desk system).
     *
     * @param userId the unique identifier of the passenger to promote
     * @return true if the promotion was successful, false if the user was not found or is not a Passenger
     */
    public boolean promoteToAirlineStaff(int userId) {
        User user = userDatabase.get(userId);
        if (user != null && user instanceof Passenger) {
            AirlineStaff staff = new AirlineStaff(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getPhone()
            );
            staff.setActive(user.isActive());
            userDatabase.put(userId, staff);
            return true;
        }
        return false;
    }

    /**
     * Clears the current user from memory, securely ending their session.
     * Prevents others from using their account if they step away from a shared computer.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Identifies exactly who is currently interacting with the application.
     * Crucial for determining if the current user has permission to view a specific booking or page.
     *
     * @return an Optional containing the active User, or empty if no user is logged in
     */
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
}
