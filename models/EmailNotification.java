package com.airline.skybooker.models;

public class EmailNotification extends Notification {
    public EmailNotification(String message) { super(message); }

    @Override
    public void send(User user) {
        System.out.println("[EMAIL to " + user.getEmail() + "]: " + message);
    }
}
