package com.airline.skybooker.managers;

import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.BoardingPass;
import com.airline.skybooker.models.BookingPassenger;
import com.airline.skybooker.exception.BookingNotFoundException;
import com.airline.skybooker.constants.AppConstants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


/**
 * Handles the process of checking in for a flight before departure.
 * Verifies that the passenger is allowed to check in and generates their boarding pass.
 */
public class CheckInManager {

    private static volatile CheckInManager instance;

    private CheckInManager() {}

    /**
     * Provides access to the single, shared CheckInManager instance.
     * Ensures all check-ins follow the exact same business rules and validation checks.
     *
     * @return the singleton CheckInManager instance
     */
    public static CheckInManager getInstance() {
        if (instance == null) {
            synchronized (CheckInManager.class) {
                if (instance == null) {
                    instance = new CheckInManager();
                }
            }
        }
        return instance;
    }

    /**
     * Verifies if a user is legally allowed to check in for their flight right now.
     * It checks if they own the booking and if the booking is confirmed.
     *
     * @param pnr         the Passenger Name Record code representing the booking
     * @param currentUser the user initiating the check-in process
     * @return the matched Booking entity if all validations pass
     * @throws IllegalStateException if any validation check fails
     */
    public Booking validateAndRetrieveBooking(String pnr, User currentUser) throws IllegalStateException {

        // find the particular booking of a user via pnr
        Booking booking = BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getPnrCode() != null && b.getPnrCode().equalsIgnoreCase(pnr))
                .findFirst()
                .orElseThrow(() -> new BookingNotFoundException("Booking with PNR " + pnr + " not found."));

        if (booking.getUserId() != currentUser.getUserId()) {
            throw new IllegalStateException("Unauthorized access. Booking does not belong to you.");
        }

        if (!booking.getStatus().equals(AppConstants.STATUS_CONFIRMED)) {
            throw new IllegalStateException("Only CONFIRMED bookings can be checked in.");
        }

        Flight flight = FlightManager.getInstance().getAllFlights().stream()
                .filter(f -> f.getFlightId() == booking.getFlightId())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Associated flight not found."));

        // Validate Check-in Window (24h to 3h before departure)
        long hoursUntilDeparture = ChronoUnit.HOURS.between(LocalDateTime.now(), flight.getDepartureTime());

        if (hoursUntilDeparture > 24) {
            throw new IllegalStateException("Check-in opens only 24 hours before departure. Please come back later.");
        }
        if (hoursUntilDeparture < 3) {
            throw new IllegalStateException("Check-in closed. Please proceed directly to the gate.");
        }

        return booking;
    }

    /**
     * Creates the actual boarding pass that the customer will print or show on their phone at the airport.
     * Collects all necessary information like seat number, departure gate, and baggage details into one document.
     *
     * @param booking           the confirmed Booking
     * @param flight            the scheduled Flight
     * @param bp                the specific passenger within the booking
     * @param baggage           the declared baggage information
     * @param specialAssistance flag indicating if wheelchair or special assistance is required
     * @return a newly constructed BoardingPass instance
     */
    public BoardingPass createBoardingPass(Booking booking, Flight flight, BookingPassenger bp, String baggage, boolean specialAssistance) {
        return new BoardingPass(
            booking.getPnrCode(),
            bp.getFullName(),
            flight.getFlightNumber(),
            flight.getOrigin().getCity(),
            flight.getDestination().getCity(),
            flight.getDepartureTime().toLocalDate().toString(),
            flight.getDepartureTime().toLocalTime().toString().substring(0, 5),
            bp.getSeatNumber(),
            flight.getDepartureGate(),
            baggage,
            specialAssistance
        );
    }

    /**
     * Updates the booking's status in the system to show that the passenger has successfully checked in.
     * This informs the flight crew that the passenger is expected at the gate.
     *
     * @param booking the Booking to transition
     */
    public void finalizeCheckInState(Booking booking) {
        booking.nextState(); // Moves from CONFIRMED -> CHECKED_IN
    }
}
