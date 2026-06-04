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
 * Manages the flight booking process.
 * This service coordinates seat selection, calculating fares, taking payments, and notifying the user.
 * It exists to keep all the steps required for a booking in one place.
 */
public class BookingService {

    private static volatile BookingService instance;

    private BookingService() {}

    /**
     * Gets the single, shared instance of this service.
     * We use a singleton so the entire application shares the same booking service.
     *
     * @return the BookingService instance
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
     * Completes a booking by locking the seats, calculating the final price, taking payment, and sending a confirmation.
     * This method exists to safely handle the transition from "selecting a flight" to "having a confirmed ticket".
     * 
     * @param booking     the booking to finalize
     * @param flight      the chosen flight
     * @param isExpress   whether the user chose faster processing
     * @param payable     the payment details
     * @param seatService the service to lock and confirm seats
     * @param promoCode   a discount code, if any
     * @return true if payment succeeds and booking is confirmed, false otherwise
     * @throws SeatLockException       if someone else took the seats while we were booking
     * @throws NetworkTimeoutException if the payment gateway takes too long
     * @throws PaymentFailureException if the payment is rejected
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
     * Cancels an entire booking, calculates the refund and penalty, and frees up the seats.
     * This exists so users can change their minds and the airline can resell the seats.
     * 
     * @param booking     the booking to cancel
     * @param flight      the flight the booking was for
     * @param seatService the service used to free the seats
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
     * Cancels the ticket for a single passenger in a group booking.
     * It frees up just their seat and refunds their portion of the fare, so the rest of the group can still fly.
     * 
     * @param booking        the booking containing the passenger
     * @param flight         the flight they were on
     * @param passengerIndex the position of the passenger in the booking's list
     * @param seatService    the service used to free the passenger's seat
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
