package com.airline.skybooker.managers;

import com.airline.skybooker.models.Booking;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton Manager responsible for Booking lifecycle.
 */
public class BookingManager {

    private static volatile BookingManager instance;
    
    // In-memory database of bookings mapped by ID
    private final Map<String, Booking> bookingDatabase;

    private BookingManager() {
        this.bookingDatabase = new ConcurrentHashMap<>();
    }

    public static BookingManager getInstance() {
        if (instance == null) {
            synchronized (BookingManager.class) {
                if (instance == null) {
                    instance = new BookingManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initiates a new booking.
     */
    public Booking initiateBooking(int userId, int flightId) {
        String bookingId = "BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking(bookingId, userId, flightId);
        bookingDatabase.put(bookingId, booking);
        System.out.println("Booking " + bookingId + " created with status: " + booking.getStatus());
        return booking;
    }

    public Optional<Booking> getBooking(String bookingId) {
        return Optional.ofNullable(bookingDatabase.get(bookingId));
    }
}
