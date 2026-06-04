package com.airline.skybooker.managers;

import com.airline.skybooker.notifications.AbstractNotification;
import com.airline.skybooker.notifications.EmailNotification;
import com.airline.skybooker.notifications.SMSNotification;
import com.airline.skybooker.notifications.WhatsAppNotification;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.BoardingPass;
import com.airline.skybooker.constants.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles sending all messages and alerts to customers.
 * It automatically chooses the best way to contact a user—like sending an email for a long receipt, or an SMS/WhatsApp for a quick flight delay warning.
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

    /**
     * Provides access to the single, shared NotificationManager instance.
     * Ensures all messages go through the same centralized system to avoid spamming the user.
     *
     * @return the singleton NotificationManager instance
     */
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
     * Sends a simple text message to the user through all of their preferred contact methods (Email, SMS, and optionally WhatsApp).
     *
     * @param user    the target User entity receiving the message
     * @param message the plain text payload to be transmitted
     */
    private void routeMessage(User user, String message) {
        if (user == null) return;
        
        for (AbstractNotification channel : activeChannels) {
            channel.send(user, message);
        }
        
        // Handle WhatsApp Opt-in dynamically
        if (user instanceof Passenger) {
            boolean optIn = ((Passenger) user).isWhatsappOptIn();
            if (optIn) {
                new WhatsAppNotification().send(user, message);
            }
        }
    }

    /**
     * Sends the customer their ticket right after they finish paying for a flight.
     * Sends a detailed receipt to their email and a quick summary to their phone.
     *
     * @param user    the user who owns the booking
     * @param booking the confirmed Booking object
     * @param flight  the scheduled Flight object
     */
    public void sendBookingConfirmation(User user, Booking booking, Flight flight) {
        if (user == null) return;
        
        // Detailed E-Ticket via Email
        String emailBody = String.format(AppConstants.MSG_E_TICKET, booking.getPnrCode(), booking.getTotalFare());
        new EmailNotification().send(user, emailBody);
        
        // Short SMS
        String smsBody = String.format(AppConstants.MSG_BOOKING_CONFIRMATION, booking.getPnrCode(), flight.getFlightNumber(), flight.getOrigin().getCity(), flight.getDestination().getCity());
        new SMSNotification().send(user, smsBody);
        
        // WhatsApp if opted in
        if (user instanceof Passenger && ((Passenger) user).isWhatsappOptIn()) {
            new WhatsAppNotification().send(user, emailBody);
        }
    }

    /**
     * Warns a customer immediately if something goes wrong with their upcoming flight, like a delay or a gate change.
     *
     * @param user      the target User entity
     * @param flight    the affected Flight
     * @param alertType the classification of the alert (e.g., "DELAYED", "GATE CHANGED")
     * @param detail    a descriptive string explaining the alert scenario
     */
    public void sendFlightAlert(User user, Flight flight, String alertType, String detail) {
        String message = String.format(AppConstants.MSG_FLIGHT_ALERT, flight.getFlightNumber(), alertType, detail);
        routeMessage(user, message);
    }
    
    /**
     * Reminds a customer to check in for their flight when the 24-hour window opens, or warns them that boarding is starting soon.
     *
     * @param user         the target User entity
     * @param booking      the associated Booking
     * @param reminderType the type or phase of the reminder (e.g., "Check-in Open")
     */
    public void sendTravelReminder(User user, Booking booking, String reminderType) {
        String message = String.format(AppConstants.MSG_TRAVEL_REMINDER, booking.getPnrCode(), reminderType);
        routeMessage(user, message);
    }

    /**
     * Keeps the customer updated when they cancel a flight and are waiting for their money back.
     *
     * @param user    the target User entity receiving the refund
     * @param booking the cancelled or modified Booking
     * @param amount  the fiat value of the processed refund
     */
    public void sendRefundLifecycle(User user, Booking booking, double amount) {
        if (user == null) return;
        
        // Email for Initiation
        String emailBody = String.format(AppConstants.MSG_REFUND_INITIATED, amount, booking.getPnrCode());
        new EmailNotification().send(user, emailBody);
        
        // SMS for Completion (Simulated)
        String smsBody = String.format(AppConstants.MSG_REFUND_COMPLETED, amount);
        new SMSNotification().send(user, smsBody);
    }

    /**
     * Emails the final digital boarding pass to the passenger after they successfully complete the online check-in process.
     *
     * @param user the target User entity
     * @param pass the constructed BoardingPass object
     */
    public void sendBoardingPass(User user, BoardingPass pass) {
        String emailBody = String.format(AppConstants.MSG_BOARDING_PASS, pass.getFormattedPass());
        new EmailNotification().send(user, emailBody);
    }
}
