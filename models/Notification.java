package com.airline.skybooker.models;

public abstract class Notification {
    protected String message;
    public Notification(String message) { this.message = message; }
    public abstract void send(User user);
}
