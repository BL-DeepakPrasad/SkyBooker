package com.airline.skybooker.services;

import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.BookingPassenger;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.User;
import com.airline.skybooker.interfaces.Payable;
import com.airline.skybooker.managers.PaymentManager;
import com.airline.skybooker.managers.NotificationManager;
import com.airline.skybooker.managers.PriorityBookingManager;
import com.airline.skybooker.managers.AuthenticationManager;
import com.airline.skybooker.exception.NetworkTimeoutException;
import com.airline.skybooker.exception.PaymentFailureException;
import com.airline.skybooker.exception.SeatLockException;
import com.airline.skybooker.states.RefundedState;
import com.airline.skybooker.constants.AppConstants;

/**
 * Service dedicated to orchestrating the complex business workflows of flight bookings.
 * Enforces the Single Responsibility Principle by decoupling payment processing, seat locking, 
 * and notifications from the basic data management of bookings.
 */
public class BookingService {

    private static volatile BookingService instance;

    private BookingService() {}

    /**
     * Retrieves the singleton instance of the BookingService.
     * Guaranteed to return a single, thread-safe instance across the application lifecycle.
     *
     * @return the singleton instance of the BookingService
     */
    public static BookingService getInstance() {
        if (instance == null) {
            synchronized (BookingService.class) {
                if (instance == null) {
                    instance = new BookingService();
                }
            }
        }
        return instance;
    }

    /**
     * Orchestrates the final phase of booking, handling seat locks, fare finalization, payment processing, and confirmation signaling.
     * Coordinates interactions between SeatService, FareCalculatorService, PaymentManager, and NotificationManager.
     * 
     * @param booking     the pending Booking to finalize
     * @param flight      the selected Flight instance
     * @param isExpress   flag indicating if express or premium processing is requested
     * @param payable     the payment method abstraction providing payment details
     * @param seatService the service handling seat reservation locks
     * @param promoCode   an optional promotional code for fare discounts
     * @return true if the payment succeeds and booking is confirmed, false otherwise
     * @throws SeatLockException       if requested seats become unavailable during the lock phase
     * @throws NetworkTimeoutException if the external payment gateway experiences a timeout
     * @throws PaymentFailureException if the payment transaction is declined or fails
     */
    public boolean processPaymentAndConfirm(Booking booking, Flight flight, 
                                            boolean isExpress, Payable payable, 
                                            SeatService seatService, String promoCode) throws SeatLockException, NetworkTimeoutException, PaymentFailureException {
        // 1. Lock Seats for all passengers
        for (BookingPassenger bp : booking.getPassengers()) {
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
        booking.setPayable(payable);
        booking.setTotalFare(finalAmount);

        // 3. Process Payment
        boolean paymentSuccess = PaymentManager.getInstance().processTransaction(payable, finalAmount);
        
        if (paymentSuccess) {
            // Confirm seats permanently
            for (BookingPassenger bp : booking.getPassengers()) {
                seatService.confirmSeat(flight.getFlightNumber(), bp.getSeatNumber());
                bp.setFarePaid(finalAmount / booking.getPassengers().size());
            }

            // 4. Confirm Booking: PAYMENT_PENDING -> CONFIRMED
            booking.nextState(); 
            
            // Notify passenger of confirmation
            User passenger = AuthenticationManager.getInstance().getCurrentUser().orElse(null);
            if (passenger != null) {
                NotificationManager.getInstance().sendBookingConfirmation(passenger, booking, flight);
                    
                // Simulate Scheduled Reminders
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

    /**
     * Cancels an active booking, computes penalty fees, processes refunds, and releases allocated seats.
     * Triggers the refund notification lifecycle for the primary user.
     * 
     * @param booking     the target Booking to completely cancel
     * @param flight      the associated Flight object containing the seats
     * @param seatService the service responsible for releasing seat allocations
     */
    public void cancelBooking(Booking booking, Flight flight, SeatService seatService) {
        if (booking.getStatus().equals(AppConstants.STATUS_CONFIRMED)) {
            System.out.println("\n[CANCELLATION] Processing cancellation for " + booking.getPnrCode());
            
            double[] refundData = FareCalculatorService.getInstance().calculateRefundAndPenalty(booking.getTotalFare());
            double refundAmount = refundData[0];
            double penalty = refundData[1];
            
            System.out.printf("[CANCELLATION] Total Fare: INR %.2f | Penalty: INR %.2f | Refund Amount: INR %.2f%n", 
                                booking.getTotalFare(), penalty, refundAmount);
            
            if (booking.getPayable() != null) {
                boolean refundSuccess = PaymentManager.getInstance().processRefund(booking.getPayable(), refundAmount);
                if (refundSuccess) {
                    booking.setState(new RefundedState());
                    
                    // Release all seats
                    for (BookingPassenger bp : booking.getPassengers()) {
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

    /**
     * Processes a partial cancellation for a specific passenger within a booking, adjusting total fares and releasing their specific seat.
     * Retains the primary booking for the remaining passengers.
     * 
     * @param booking        the Booking containing the passenger to be removed
     * @param flight         the associated Flight object
     * @param passengerIndex the zero-based index of the passenger in the booking's passenger list
     * @param seatService    the service handling seat releases
     */
    public void cancelSpecificPassenger(Booking booking, Flight flight, int passengerIndex, SeatService seatService) {
        if (passengerIndex < 0 || passengerIndex >= booking.getPassengers().size()) return;
        BookingPassenger bp = booking.getPassengers().get(passengerIndex);
        if (bp.isCancelled()) return;

        double[] refundData = FareCalculatorService.getInstance().calculateRefundAndPenalty(bp.getFarePaid());
        double refundAmount = refundData[0];
        double penalty = refundData[1];
        
        System.out.printf("[PARTIAL CANCELLATION] %s | Penalty: INR %.2f | Refund: INR %.2f%n", 
                          bp.getFullName(), penalty, refundAmount);
                          
        if (booking.getPayable() != null) {
            boolean refundSuccess = PaymentManager.getInstance().processRefund(booking.getPayable(), refundAmount);
            if (refundSuccess) {
                bp.setCancelled(true);
                seatService.releaseSeat(flight.getFlightNumber(), bp.getSeatNumber());
                booking.setTotalFare(booking.getTotalFare() - bp.getFarePaid());
            }
        }
    }
}
