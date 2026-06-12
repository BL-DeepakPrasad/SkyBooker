package com.airline.skybooker.managers;

import com.airline.skybooker.models.Booking;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

import com.airline.skybooker.models.Flight;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.exception.SeatLockException;
import java.util.ArrayList;
import com.airline.skybooker.models.BookingPassenger;
import com.airline.skybooker.constants.AppConstants;

/**
 * Stores and manages all the flight bookings in the system.
 * Acts as the central database for creating new bookings and finding existing ones by ID or by the user who made them.
 */
public class BookingManager {

    private static volatile BookingManager instance;
    
    // In-memory database of bookings mapped by ID
    private final Map<String, Booking> bookingDatabase;

    private BookingManager() {
        this.bookingDatabase = new ConcurrentHashMap<>();
    }

    /**
     * Provides access to the single, shared BookingManager instance.
     * Ensures all new bookings are saved into the same central list.
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
     * Creates a brand new, empty booking for a user on a specific flight.
     * The booking starts in a "PENDING" state until the user adds passengers and pays.
     *
     * @param userId   the unique identifier of the user initiating the booking
     * @param flightId the unique identifier of the target flight
     * @return a freshly initialized Booking instance with a pending status
     */
    public Booking initiateBooking(int userId, int flightId) {
        String bookingId = AppConstants.BOOKING_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking(bookingId, userId, flightId);

        // Generate PNR immediately upon creation (not after payment)
        String pnr = "PNR" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        booking.setPnrCode(pnr);

        bookingDatabase.put(bookingId, booking);
        System.out.println("Booking " + bookingId + " created with status: " + booking.getStatus() + ", PNR: " + pnr);
        return booking;
    }

    /**
     * Finds a specific booking using its unique booking ID (e.g., "BKG-A1B2C3D4").
     * Used when a customer wants to view or manage an upcoming trip.
     *
     * @param bookingId the unique string ID of the booking
     * @return an Optional containing the corresponding Booking, or empty if not found
     */
    public Optional<Booking> getBooking(String bookingId) {
        return Optional.ofNullable(bookingDatabase.get(bookingId));
    }

    /**
     * Returns a list of every single booking ever made in the system.
     * Primarily used by the AnalyticsManager to calculate total revenue and other reports.
     *
     * @return a list containing all active and inactive booking records
     */
    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookingDatabase.values());
    }

    /**
     * Finds all bookings made by a specific customer.
     * Used to populate the "My Trips" or "Booking History" page on the user's dashboard.
     *
     * @param userId the unique identifier of the user
     * @return a list of bookings linked to the provided user ID
     */
    public List<Booking> getBookingsForUser(int userId) {
        return bookingDatabase.values().stream()
                .filter(b -> b.getUserId() == userId)
                .collect(Collectors.toList());
    }

    // Cancellation and Payment methods have been moved to BookingService 
    // to strictly enforce the Single Responsibility Principle.

    /**
     * Updates the name, passport, or meal preference for a passenger on an existing booking.
     * Helpful if a customer made a typo during checkout or decided they want an in-flight meal later.
     *
     * @param booking        the target Booking object
     * @param passengerIndex the zero-based index of the passenger
     * @param newName        the updated full name, or empty string to retain existing
     * @param newPassport    the updated passport number, or empty string to retain existing
     * @param newMeal        the updated meal upgrade preference flag
     */
    public void modifyPassengerDetails(Booking booking, int passengerIndex, String newName, String newPassport, boolean newMeal) {

        List<BookingPassenger> listOfPassenger = booking.getPassengers();

        if (passengerIndex < 0 || passengerIndex >= listOfPassenger.size()) return;

        BookingPassenger bookingPassenger = listOfPassenger.get(passengerIndex);

        if (bookingPassenger.isCancelled()) return;
        if (!newName.isEmpty()) bookingPassenger.setFullName(newName);
        if (!newPassport.isEmpty()) bookingPassenger.setPassportNumber(newPassport);
        bookingPassenger.setMealUpgrade(newMeal);
    }

    /**
     * Moves a passenger from their current seat to a new one on the plane.
     * Safely releases their old seat so someone else can buy it, and locks the new seat for them.
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

        BookingPassenger bookingPassenger = booking.getPassengers().get(passengerIndex);
        if (bookingPassenger.isCancelled()) return false;
        
        try {
            if (seatService.lockSeat(flight.getFlightNumber(), newSeat)) {
                seatService.releaseSeat(flight.getFlightNumber(), bookingPassenger.getSeatNumber());
                seatService.confirmSeat(flight.getFlightNumber(), newSeat);
                bookingPassenger.setSeatNumber(newSeat);
                return true;
            }
        } catch (SeatLockException e) {
            System.out.println("Cannot change seat: " + e.getMessage());
        }
        return false;
    }


}
