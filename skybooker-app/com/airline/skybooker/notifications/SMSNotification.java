package com.airline.skybooker.notifications;

import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;

public class SMSNotification extends AbstractNotification {
    
    @Override
    public boolean send(User user, String message) {
        String phone = "";
        if (user instanceof Passenger) {
            phone = ((Passenger) user).getPhone();
        } else {
            return false; // Cannot send SMS if no phone number
        }

        System.out.println("\n[SMS GATEWAY] Sending text to: " + phone);
        System.out.println("Message: " + message);
        System.out.println("[SMS GATEWAY] SMS delivered successfully.");
        return true;
    }
}
