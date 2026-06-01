package com.airline.skybooker.managers;

import com.airline.skybooker.notifications.AbstractNotification;
import com.airline.skybooker.notifications.EmailNotification;
import com.airline.skybooker.notifications.SMSNotification;
import com.airline.skybooker.notifications.WhatsAppNotification;
import com.airline.skybooker.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton Manager responsible for routing notifications.
 * Demonstrates Strategy/Polymorphic routing of notifications.
 */
public class NotificationManager {

    private static volatile NotificationManager instance;
    private final List<AbstractNotification> activeChannels;

    private NotificationManager() {
        activeChannels = new ArrayList<>();
        // By default, activate Email and SMS. WhatsApp is optional.
        activeChannels.add(new EmailNotification());
        activeChannels.add(new SMSNotification());
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            synchronized (NotificationManager.class) {
                if (instance == null) {
                    instance = new NotificationManager();
                }
            }
        }
        return instance;
    }

    /**
     * Internal method to route a message to the active channels for a user.
     */
    private void routeMessage(User user, String message) {
        if (user == null) return;
        
        for (AbstractNotification channel : activeChannels) {
            channel.send(user, message);
        }
        
        // Handle WhatsApp Opt-in dynamically
        if (user instanceof com.airline.skybooker.models.Passenger) {
            boolean optIn = ((com.airline.skybooker.models.Passenger) user).isWhatsappOptIn();
            if (optIn) {
                new WhatsAppNotification().send(user, message);
            }
        }
    }

    /**
     * 12.1 Booking Confirmation Template
     */
    public void sendBookingConfirmation(User user, com.airline.skybooker.models.Booking booking, com.airline.skybooker.models.Flight flight) {
        if (user == null) return;
        
        // Detailed E-Ticket via Email
        String emailBody = String.format(
            "Booking CONFIRMED!\nPNR: %s\nFlight: %s (%s -> %s)\nDeparture: %s\nTotal Paid: $%.2f (Receipt Attached)",
            booking.getPnrCode(), flight.getFlightNumber(), flight.getOrigin().getIataCode(), 
            flight.getDestination().getIataCode(), flight.getDepartureTime(), booking.getTotalFare()
        );
        new EmailNotification().send(user, emailBody);
        
        // Simple PNR via SMS
        String smsBody = "Skybooker: Your flight is confirmed. PNR: " + booking.getPnrCode() + ". Have a safe trip!";
        new SMSNotification().send(user, smsBody);
        
        // WhatsApp if opted in
        if (user instanceof com.airline.skybooker.models.Passenger && ((com.airline.skybooker.models.Passenger) user).isWhatsappOptIn()) {
            new WhatsAppNotification().send(user, emailBody);
        }
    }

    /**
     * 12.2 Flight Update Notification
     */
    public void sendFlightAlert(User user, com.airline.skybooker.models.Flight flight, String alertType, String detail) {
        String message = String.format("URGENT: Flight %s is %s. %s", flight.getFlightNumber(), alertType, detail);
        routeMessage(user, message);
    }
    
    /**
     * 12.2 Travel Reminders (Check-in / Boarding)
     */
    public void sendTravelReminder(User user, com.airline.skybooker.models.Booking booking, String reminderType) {
        String message = String.format("REMINDER: Your flight (PNR: %s) %s", booking.getPnrCode(), reminderType);
        routeMessage(user, message);
    }

    /**
     * 12.3 Refund Lifecycle Notification
     */
    public void sendRefundLifecycle(User user, com.airline.skybooker.models.Booking booking, double amount) {
        if (user == null) return;
        
        // Email for Initiation
        String emailBody = String.format("Cancellation Confirmed for PNR: %s.\nA refund of $%.2f has been INITIATED to your original payment method.", booking.getPnrCode(), amount);
        new EmailNotification().send(user, emailBody);
        
        // SMS for Completion
        String smsBody = String.format("Skybooker: Refund of $%.2f for PNR %s is COMPLETED.", amount, booking.getPnrCode());
        new SMSNotification().send(user, smsBody);
    }
}
