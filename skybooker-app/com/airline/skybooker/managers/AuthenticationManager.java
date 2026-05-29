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

/**
 * Singleton Manager responsible for User Authentication and Registration.
 */
public class AuthenticationManager {

    private static volatile AuthenticationManager instance;
    private final Map<String, User> userDatabase; // Email -> User
    private final AtomicInteger idGenerator;
    private User currentUser;

    private AuthenticationManager() {
        this.userDatabase = new ConcurrentHashMap<>();
        this.idGenerator = new AtomicInteger(1);
        
        // Mock Admin Account
        registerAdmin("System Admin", "admin@skybooker.com", "admin123", "555-0000");
        // Mock Staff Account
        registerStaff("Flight Ops", "staff@skybooker.com", "staff123", "555-1111");
    }

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

    public Passenger registerPassenger(String fullName, String email, String password, String phone, String passport, String nationality) {
        if (userDatabase.containsKey(email.toLowerCase())) {
            throw new IllegalArgumentException("Email already registered!");
        }
        
        String passwordHash = PasswordUtils.hashPassword(password);
        
        Passenger passenger = new Passenger(idGenerator.getAndIncrement(), fullName, email.toLowerCase(), passwordHash, phone, passport, nationality);
        userDatabase.put(passenger.getEmail(), passenger);
        return passenger;
    }

    private void registerAdmin(String name, String email, String pass, String phone) {
        Admin admin = new Admin(idGenerator.getAndIncrement(), name, email.toLowerCase(), PasswordUtils.hashPassword(pass), phone);
        userDatabase.put(admin.getEmail(), admin);
    }

    private void registerStaff(String name, String email, String pass, String phone) {
        AirlineStaff staff = new AirlineStaff(idGenerator.getAndIncrement(), name, email.toLowerCase(), PasswordUtils.hashPassword(pass), phone);
        userDatabase.put(staff.getEmail(), staff);
    }

    public boolean login(String email, String password) {
        User user = userDatabase.get(email.toLowerCase());
        if (user != null) {
            if (PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                this.currentUser = user;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
}
