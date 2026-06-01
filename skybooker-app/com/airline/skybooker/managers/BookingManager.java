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
import com.airline.skybooker.services.FareCalculatorService;
import com.airline.skybooker.enums.BookingPriority;
import com.airline.skybooker.exception.SeatLockException;
import com.airline.skybooker.states.RefundedState;

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
     * Service method to cancel a booking and process refund.
     */
    public void cancelBooking(Booking booking) {
        if (booking.getStatus().equals("CONFIRMED")) {
            System.out.println("\n[CANCELLATION] Processing cancellation for " + booking.getPnrCode());
            
            double[] refundData = FareCalculatorService.getInstance().calculateRefundAndPenalty(booking.getTotalFare());
            double refundAmount = refundData[0];
            double penalty = refundData[1];
            
            System.out.printf("[CANCELLATION] Total Fare: $%.2f | Penalty: $%.2f | Refund Amount: $%.2f%n", 
                               booking.getTotalFare(), penalty, refundAmount);
            
            if (booking.getPaymentStrategy() != null) {
                boolean refundSuccess = PaymentManager.getInstance().processRefund(booking.getPaymentStrategy(), refundAmount);
                if (refundSuccess) {
                    booking.setState(new RefundedState());
                    return;
                }
            }
        }
        booking.cancel();
    }

    /**
     * God Method to orchestrate locking, pricing, payment, and priority routing.
     */
    public boolean processPaymentAndConfirm(Booking booking, String flightNumber, String seatNum, 
                                            boolean isExpress, PaymentStrategy strategy, 
                                            double baseFare, SeatService seatService, String promoCode) throws SeatLockException {
        // 1. Lock Seat
        if (!seatService.lockSeat(flightNumber, seatNum)) {
            return false;
        }
        
        // Transition: PASSENGER_DETAILS -> SEAT_SELECTED
        booking.nextState(); 
        
        // Transition: SEAT_SELECTED -> PAYMENT_PENDING
        booking.nextState();
        
        // 2. Calculate Final Fare & Priority via Service
        double finalAmount = FareCalculatorService.getInstance().calculateFinalFare(booking, baseFare, isExpress, promoCode);
        
        // Save strategy for future refunds
        booking.setPaymentStrategy(strategy);
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
