package com.airline.skybooker.notifications;

import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;

public class EmailNotification extends AbstractNotification {
    
    @Override
    public boolean send(User user, String message) {
        String email = user.getEmail();
        System.out.println("\n[EMAIL SYSTEM] Sending email to: " + email);
        System.out.println("Subject: Skybooker Important Update");
        System.out.println("Dear " + getRecipientName(user) + ",");
        System.out.println(message);
        System.out.println("[EMAIL SYSTEM] Message delivered successfully.");
        return true;
    }
}
