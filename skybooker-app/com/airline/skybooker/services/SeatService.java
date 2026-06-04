package com.airline.skybooker.services;

import com.airline.skybooker.exception.SeatLockException;
import com.airline.skybooker.models.Seat;
import com.airline.skybooker.enums.SeatType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages aircraft seating.
 * This service tracks which seats are available, locked, or booked, and ensures
 * two people cannot book the same seat at the same time.
 */
public class SeatService {
    // Represents a database repository mapping flights to their seats
    private final Map<String, List<Seat>> flightSeats = new ConcurrentHashMap<>();

    public SeatService() {
        // Mocking database population
        initializeMockSeats("AI-101");
        initializeMockSeats("IG-202");
        initializeMockSeats("AI-303");
    }

    private void initializeMockSeats(String flightNumber) {
        com.airline.skybooker.models.Flight flight = com.airline.skybooker.managers.FlightManager.getInstance().getFlightByNumber(flightNumber).orElse(null);
        
        if (flight != null) {
            int capacity = flight.getTotalCapacity();
            int available = flight.getAvailableSeats();
            initializeAircraftLayout(flightNumber, capacity);
            
            List<Seat> seats = flightSeats.get(flightNumber);
            int seatsToBook = capacity - available;
            
            // Randomly book seats so the seat map looks realistic, but for simplicity we'll just book the first N seats.
            for (int i = 0; i < seatsToBook && i < seats.size(); i++) {
                seats.get(i).setBooked(true);
            }
        } else {
            initializeAircraftLayout(flightNumber, 120); 
        }
    }

    /**
     * Generates a basic seat layout for a flight.
     * This prepares the flight so users can view and pick seats.
     * 
     * @param flightNumber the flight to set up
     * @param totalSeats   how many total seats the plane has
     */
    public void initializeAircraftLayout(String flightNumber, int totalSeats) {
        List<Seat> seats = new ArrayList<>(totalSeats);

        SeatType[] seatTypes = {
                SeatType.WINDOW,
                SeatType.MIDDLE,
                SeatType.AISLE,
                SeatType.AISLE,
                SeatType.MIDDLE,
                SeatType.WINDOW
        };

        for (int seatNumber = 0; seatNumber < totalSeats; seatNumber++) {
            int row = (seatNumber / 6) + 1;
            char seatLetter = (char) ('A' + (seatNumber % 6));

            seats.add(new Seat(row + String.valueOf(seatLetter),
                    seatTypes[seatNumber % 6]));
        }

        flightSeats.put(flightNumber, seats);
    }

    /**
     * Prints a visual layout of the airplane seats to the console.
     * This helps the user pick an available seat.
     * 
     * @param flightNumber the flight to display
     */
    public void displaySeatMap(String flightNumber) {
        List<Seat> seats = flightSeats.get(flightNumber);
        if (seats == null) {
            System.out.println("No seat map available for " + flightNumber);
            return;
        }

        System.out.println("\n--- SEAT MAP (" + flightNumber + ") ---");
        System.out.println("Legend: [ ] Available | [L] Locked | [X] Booked");
        for (Seat seat : seats) {
            String status = seat.isBooked() ? "[X]" : (seat.isLocked() ? "[L]" : "[ ]");
            String typeStr = seat.getType() == SeatType.WINDOW ? "Window" : (seat.getType() == SeatType.AISLE ? "Aisle " : "Middle");
            System.out.printf("%s %-3s (%s)%n", status, seat.getSeatNumber(), typeStr);
        }
    }

    /**
     * Temporarily reserves a seat for a user who is in the process of paying.
     * This stops someone else from taking the seat right before checkout.
     * 
     * @param flightNumber the flight number
     * @param seatNumber   the seat (like "1A")
     * @return true if the seat was successfully locked
     * @throws SeatLockException if the seat is already taken or doesn't exist
     */
    public boolean lockSeat(String flightNumber, String seatNumber) throws SeatLockException {
        List<Seat> seats = flightSeats.get(flightNumber);
        if (seats == null) throw new SeatLockException("Flight not found in Seat Database.");

        for (Seat seat : seats) {
            if (seat.getSeatNumber().equalsIgnoreCase(seatNumber)) {
                // Thread-safe lock check
                synchronized (seat) {
                    if (seat.isBooked()) {
                        throw new SeatLockException("Seat " + seatNumber + " is already permanently booked.");
                    }
                    if (seat.isLocked()) {
                        throw new SeatLockException("Seat " + seatNumber + " is temporarily locked by another user.");
                    }
                    
                    seat.setLocked(true);
                    return true;
                }
            }
        }
        throw new SeatLockException("Seat " + seatNumber + " does not exist on this aircraft.");
    }

    /**
     * Permanently marks a locked seat as booked after a successful payment.
     * 
     * @param flightNumber the flight number
     * @param seatNumber   the seat to confirm
     */
    public void confirmSeat(String flightNumber, String seatNumber) {
        List<Seat> seats = flightSeats.get(flightNumber);
        if (seats != null) {
            for (Seat seat : seats) {
                if (seat.getSeatNumber().equalsIgnoreCase(seatNumber)) {
                    synchronized (seat) {
                        seat.setBooked(true);
                        seat.setLocked(false);
                    }
                    return;
                }
            }
        }
    }

    /**
     * Frees a seat up for anyone to book again.
     * Used when a payment fails or a booking is cancelled.
     * 
     * @param flightNumber the flight number
     * @param seatNumber   the seat to free up
     */
    public void releaseSeat(String flightNumber, String seatNumber) {
        List<Seat> seats = flightSeats.get(flightNumber);
        if (seats != null) {
            for (Seat seat : seats) {
                if (seat.getSeatNumber().equalsIgnoreCase(seatNumber)) {
                    synchronized (seat) {
                        seat.setBooked(false);
                        seat.setLocked(false);
                    }
                    return;
                }
            }
        }
    }

    /**
     * Checks if a seat is valid and available to be selected.
     * 
     * @param flightNumber the flight number
     * @param seatNumber   the seat to check
     * @return true if the seat exists and is available
     */
    public boolean isValidSeat(String flightNumber, String seatNumber) {
        List<Seat> seats = flightSeats.get(flightNumber);
        if (seats == null) return false;
        
        for (Seat seat : seats) {
            if (seat.getSeatNumber().equalsIgnoreCase(seatNumber)) {
                return !seat.isBooked();
            }
        }
        return false;
    }
}
