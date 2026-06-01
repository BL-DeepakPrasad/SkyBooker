package com.airline.skybooker.notifications;

import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;

public class WhatsAppNotification extends AbstractNotification {
    
    @Override
    public boolean send(User user, String message) {
        String phone = "";
        if (user instanceof Passenger) {
            phone = ((Passenger) user).getPhone();
        } else {
            return false; // Cannot send WhatsApp if no phone number
        }

        System.out.println("\n[WHATSAPP BUSINESS API] Sending message to: " + phone);
        System.out.println("Message: \uD83D\uDEEB SKYBOOKER UPDATE: " + message);
        System.out.println("[WHATSAPP BUSINESS API] Message delivered securely.");
        return true;
    }
}
