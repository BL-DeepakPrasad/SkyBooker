package com.airline.skybooker.managers;

import com.airline.skybooker.models.Booking;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

import com.airline.skybooker.models.Flight;
import com.airline.skybooker.payments.PaymentStrategy;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.services.FareCalculatorService;
import com.airline.skybooker.enums.BookingPriority;
import com.airline.skybooker.exception.SeatLockException;
import com.airline.skybooker.states.RefundedState;
import com.airline.skybooker.models.User;
import com.airline.skybooker.managers.NotificationManager;
import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.managers.AuthenticationManager;
import java.util.ArrayList;
import com.airline.skybooker.exception.NetworkTimeoutException;
import com.airline.skybooker.exception.PaymentFailureException;

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

    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookingDatabase.values());
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
    public void cancelBooking(Booking booking, Flight flight, SeatService seatService) {
        if (booking.getStatus().equals("CONFIRMED")) {
            System.out.println("\n[CANCELLATION] Processing cancellation for " + booking.getPnrCode());
            
            double[] refundData = FareCalculatorService.getInstance().calculateRefundAndPenalty(booking.getTotalFare());
            double refundAmount = refundData[0];
            double penalty = refundData[1];
            
            System.out.printf("[CANCELLATION] Total Fare: INR %.2f | Penalty: INR %.2f | Refund Amount: INR %.2f%n", 
                                booking.getTotalFare(), penalty, refundAmount);
            
            if (booking.getPaymentStrategy() != null) {
                boolean refundSuccess = PaymentManager.getInstance().processRefund(booking.getPaymentStrategy(), refundAmount);
                if (refundSuccess) {
                    booking.setState(new RefundedState());
                    
                    // Release all seats
                    for (com.airline.skybooker.models.BookingPassenger bp : booking.getPassengers()) {
                        if (!bp.isCancelled()) {
                            seatService.releaseSeat(flight.getFlightNumber(), bp.getSeatNumber());
                            bp.setCancelled(true);
                        }
                    }
                    
                    // Notify passenger of refund
                    User passenger = AuthenticationManager.getInstance().getCurrentUser().orElse(null);
                    if (passenger != null) {
                        NotificationManager.getInstance().sendRefundLifecycle(passenger, booking, refundAmount);
                    }
                    return;
                }
            }
        }
        booking.cancel();
    }

    public void cancelSpecificPassenger(Booking booking, Flight flight, int passengerIndex, SeatService seatService) {
        if (passengerIndex < 0 || passengerIndex >= booking.getPassengers().size()) return;
        com.airline.skybooker.models.BookingPassenger bp = booking.getPassengers().get(passengerIndex);
        if (bp.isCancelled()) return;

        double[] refundData = FareCalculatorService.getInstance().calculateRefundAndPenalty(bp.getFarePaid());
        double refundAmount = refundData[0];
        double penalty = refundData[1];
        
        System.out.printf("[PARTIAL CANCELLATION] %s | Penalty: INR %.2f | Refund: INR %.2f%n", 
                          bp.getFullName(), penalty, refundAmount);
                          
        if (booking.getPaymentStrategy() != null) {
            boolean refundSuccess = PaymentManager.getInstance().processRefund(booking.getPaymentStrategy(), refundAmount);
            if (refundSuccess) {
                bp.setCancelled(true);
                seatService.releaseSeat(flight.getFlightNumber(), bp.getSeatNumber());
                booking.setTotalFare(booking.getTotalFare() - bp.getFarePaid());
            }
        }
    }

    public void modifyPassengerDetails(Booking booking, int passengerIndex, String newName, String newPassport, boolean newMeal) {
        if (passengerIndex < 0 || passengerIndex >= booking.getPassengers().size()) return;
        com.airline.skybooker.models.BookingPassenger bp = booking.getPassengers().get(passengerIndex);
        if (bp.isCancelled()) return;
        
        if (!newName.isEmpty()) bp.setFullName(newName);
        if (!newPassport.isEmpty()) bp.setPassportNumber(newPassport);
        bp.setMealUpgrade(newMeal);
    }

    public boolean changePassengerSeat(Booking booking, Flight flight, int passengerIndex, String newSeat, SeatService seatService) {
        if (passengerIndex < 0 || passengerIndex >= booking.getPassengers().size()) return false;
        com.airline.skybooker.models.BookingPassenger bp = booking.getPassengers().get(passengerIndex);
        if (bp.isCancelled()) return false;
        
        try {
            if (seatService.lockSeat(flight.getFlightNumber(), newSeat)) {
                seatService.releaseSeat(flight.getFlightNumber(), bp.getSeatNumber());
                seatService.confirmSeat(flight.getFlightNumber(), newSeat);
                bp.setSeatNumber(newSeat);
                return true;
            }
        } catch (SeatLockException e) {
            System.out.println("Cannot change seat: " + e.getMessage());
        }
        return false;
    }

    /**
     * God Method to orchestrate locking, pricing, payment, and priority routing.
     */
    public boolean processPaymentAndConfirm(Booking booking, Flight flight, 
                                            boolean isExpress, PaymentStrategy strategy, 
                                            SeatService seatService, String promoCode) throws SeatLockException, NetworkTimeoutException, PaymentFailureException {
        // 1. Lock Seats for all passengers
        for (com.airline.skybooker.models.BookingPassenger bp : booking.getPassengers()) {
            if (!seatService.lockSeat(flight.getFlightNumber(), bp.getSeatNumber())) {
                throw new SeatLockException("Seat " + bp.getSeatNumber() + " is no longer available.");
            }
        }
        
        // Transition: PASSENGER_DETAILS -> SEAT_SELECTED
        booking.nextState(); 
        
        // Transition: SEAT_SELECTED -> PAYMENT_PENDING
        booking.nextState();
        
        boolean isDomestic = flight.getOrigin().getCountry().equalsIgnoreCase(flight.getDestination().getCountry());
        
        // 2. Calculate Final Fare & Priority via Service
        double finalAmount = FareCalculatorService.getInstance().calculateFinalFare(booking, flight.getBasePrice(), isExpress, promoCode, isDomestic);
        
        // Save strategy for future refunds
        booking.setPaymentStrategy(strategy);
        booking.setTotalFare(finalAmount);

        // 3. Process Payment
        boolean success = PaymentManager.getInstance().processTransaction(strategy, finalAmount);
        
        if (success) {
            // Confirm seats permanently
            for (com.airline.skybooker.models.BookingPassenger bp : booking.getPassengers()) {
                seatService.confirmSeat(flight.getFlightNumber(), bp.getSeatNumber());
                // For simplicity in this demo, just assign average fare to each passenger
                bp.setFarePaid(finalAmount / booking.getPassengers().size());
            }

            // 4. Confirm Booking: PAYMENT_PENDING -> CONFIRMED
            booking.nextState(); 
            
            // Notify passenger of confirmation
            User passenger = AuthenticationManager.getInstance().getCurrentUser().orElse(null);
            if (passenger != null) {
                NotificationManager.getInstance().sendBookingConfirmation(passenger, booking, flight);
                    
                // Simulate Scheduled Reminders (12.2)
                NotificationManager.getInstance().sendTravelReminder(passenger, booking, "Check-in opens in 24 Hours!");
                NotificationManager.getInstance().sendTravelReminder(passenger, booking, "Boarding starts in 3 Hours!");
            }
            
            // 5. Add to Priority Processing Queue
            PriorityBookingManager.getInstance().enqueueBooking(booking);
            PriorityBookingManager.getInstance().processQueue();
            return true;
        } else {
            booking.cancel();
            return false;
        }
    }
}
