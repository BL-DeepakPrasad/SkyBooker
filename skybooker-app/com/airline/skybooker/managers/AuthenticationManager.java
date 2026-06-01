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
 * Singleton Manager responsible for User Authentication and Registration.
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

    private void initializeMockAdmin() {
        registerAdmin("System Admin", "admin@skybooker.com", "admin123");
    }

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

    public boolean login(String email, String password) {
        Integer id = emailToIdMap.get(email.toLowerCase());
        if (id != null) {
            User user = userDatabase.get(id);
            if (PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                this.currentUser = user;
                return true;
            }
        }
        return false;
    }

    public Optional<User> getUserById(int userId) {
        return Optional.ofNullable(userDatabase.get(userId));
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userDatabase.values());
    }

    public void logout() {
        this.currentUser = null;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
}
