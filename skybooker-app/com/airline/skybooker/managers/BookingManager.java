package com.airline.skybooker.managers;

import com.airline.skybooker.models.Booking;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

import com.airline.skybooker.models.Flight;
import com.airline.skybooker.interfaces.Payable;
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
import com.airline.skybooker.models.BookingPassenger;
import com.airline.skybooker.exception.PaymentFailureException;
import com.airline.skybooker.constants.AppConstants;

/**
 * Orchestrator handling the lifecycle of flight bookings, from initialization through payment and cancellation.
 * Interacts with seat services and payment gateways to ensure transactional integrity.
 */
public class BookingManager {

    private static volatile BookingManager instance;
    
    // In-memory database of bookings mapped by ID
    private final Map<String, Booking> bookingDatabase;

    private BookingManager() {
        this.bookingDatabase = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves the singleton instance of the BookingManager.
     *
     * @return the singleton BookingManager instance
     */
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
     * Initializes a new booking record mapped to a specific user and flight combination.
     *
     * @param userId   the unique identifier of the user initiating the booking
     * @param flightId the unique identifier of the target flight
     * @return a freshly initialized Booking instance with a pending status
     */
    public Booking initiateBooking(int userId, int flightId) {
        String bookingId = AppConstants.BOOKING_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking(bookingId, userId, flightId);
        bookingDatabase.put(bookingId, booking);
        System.out.println("Booking " + bookingId + " created with status: " + booking.getStatus());
        return booking;
    }

    /**
     * Fetches a booking entity using its unique booking identifier.
     *
     * @param bookingId the unique string ID of the booking
     * @return an Optional containing the corresponding Booking, or empty if not found
     */
    public Optional<Booking> getBooking(String bookingId) {
        return Optional.ofNullable(bookingDatabase.get(bookingId));
    }

    /**
     * Retrieves the complete in-memory registry of all bookings.
     *
     * @return a list containing all active and inactive booking records
     */
    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookingDatabase.values());
    }

    /**
     * Filters and retrieves all booking records associated with a specific user profile.
     *
     * @param userId the unique identifier of the user
     * @return a list of bookings linked to the provided user ID
     */
    public List<Booking> getBookingsForUser(int userId) {
        return bookingDatabase.values().stream()
                .filter(b -> b.getUserId() == userId)
                .collect(Collectors.toList());
    }

    // Cancellation and Payment methods have been moved to BookingOrchestratorService 
    // to strictly enforce the Single Responsibility Principle.

    /**
     * Updates personal details and preferences for a specific passenger within an active booking.
     *
     * @param booking        the target Booking object
     * @param passengerIndex the zero-based index of the passenger
     * @param newName        the updated full name, or empty string to retain existing
     * @param newPassport    the updated passport number, or empty string to retain existing
     * @param newMeal        the updated meal upgrade preference flag
     */
    public void modifyPassengerDetails(Booking booking, int passengerIndex, String newName, String newPassport, boolean newMeal) {
        if (passengerIndex < 0 || passengerIndex >= booking.getPassengers().size()) return;
        BookingPassenger bp = booking.getPassengers().get(passengerIndex);
        if (bp.isCancelled()) return;
        
        if (!newName.isEmpty()) bp.setFullName(newName);
        if (!newPassport.isEmpty()) bp.setPassportNumber(newPassport);
        bp.setMealUpgrade(newMeal);
    }

    /**
     * Attempts to reallocate a passenger to a new seat, releasing the old seat upon success.
     *
     * @param booking        the target Booking
     * @param flight         the Flight containing the seat map
     * @param passengerIndex the zero-based index of the passenger to move
     * @param newSeat        the desired new seat identifier (e.g., "12A")
     * @param seatService    the seat management service validating the exchange
     * @return true if the seat swap was successfully completed, false otherwise
     */
    public boolean changePassengerSeat(Booking booking, Flight flight, int passengerIndex, String newSeat, SeatService seatService) {
        if (passengerIndex < 0 || passengerIndex >= booking.getPassengers().size()) return false;
        BookingPassenger bp = booking.getPassengers().get(passengerIndex);
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

    // ProcessPaymentAndConfirm moved to BookingOrchestratorService to enforce SRP
}
