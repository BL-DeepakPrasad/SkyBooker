package com.airline.skybooker.managers;

import com.airline.skybooker.models.Booking;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

import com.airline.skybooker.payments.PaymentStrategy;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.enums.BookingPriority;
import com.airline.skybooker.exception.SeatLockException;

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

    /**
     * Retrieves all bookings for a specific user using Java Streams.
     */
    public List<Booking> getBookingsForUser(int userId) {
        return bookingDatabase.values().stream()
                .filter(b -> b.getUserId() == userId)
                .collect(Collectors.toList());
    }

    /**
     * Service method to cancel a booking.
     */
    public void cancelBooking(Booking booking) {
        booking.cancel();
    }

    /**
     * God Method to orchestrate locking, pricing, payment, and priority routing.
     */
    public boolean processPaymentAndConfirm(Booking booking, String flightNumber, String seatNum, 
                                            boolean isExpress, PaymentStrategy strategy, 
                                            double baseFare, SeatService seatService) throws SeatLockException {
        // 1. Lock Seat
        if (!seatService.lockSeat(flightNumber, seatNum)) {
            return false;
        }
        
        // Transition: PASSENGER_DETAILS -> SEAT_SELECTED
        booking.nextState(); 
        
        // Transition: SEAT_SELECTED -> PAYMENT_PENDING
        booking.nextState();
        
        // 2. Calculate Final Fare & Priority
        double finalAmount = baseFare;
        if (isExpress) {
            booking.setPriority(BookingPriority.EXPRESS);
            finalAmount += 25.0;
        } else {
            booking.setPriority(BookingPriority.REGULAR);
        }
        
        booking.setTotalFare(finalAmount);

        // 3. Process Payment
        boolean success = PaymentManager.getInstance().processTransaction(strategy, finalAmount);
        
        if (success) {
            // 4. Confirm Booking
            booking.nextState(); 
            
            // 5. Route to Priority Queue
            PriorityBookingManager.getInstance().enqueueBooking(booking);
            PriorityBookingManager.getInstance().processQueue();
            return true;
        } else {
            booking.cancel();
            return false;
        }
    }
}
