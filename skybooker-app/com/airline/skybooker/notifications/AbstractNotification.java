package com.airline.skybooker.notifications;

import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;

/**
 * Abstract base class for all notification strategies.
 */
public abstract class AbstractNotification {
    
    /**
     * Sends a notification to a specific user.
     * 
     * @param user The user receiving the notification
     * @param message The message content
     * @return true if successfully sent, false otherwise
     */
    public abstract boolean send(User user, String message);

    /**
     * Helper to get the user's name securely.
     */
    protected String getRecipientName(User user) {
        if (user instanceof Passenger) {
            return ((Passenger) user).getFullName();
        }
        return "User";
    }
}
