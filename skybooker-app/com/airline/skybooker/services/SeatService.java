package com.airline.skybooker.services;

import com.airline.skybooker.exception.SeatLockException;
import com.airline.skybooker.models.Seat;
import com.airline.skybooker.enums.SeatType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrator for aircraft seating assignments, handling concurrent inventory allocation and visualization.
 * Ensures thread-safe mutations against the underlying seat repository during high-concurrency reservation scenarios.
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
        List<Seat> seats = new ArrayList<>();
        seats.add(new Seat("1A", SeatType.WINDOW));  
        seats.add(new Seat("1B", SeatType.MIDDLE)); 
        seats.add(new Seat("1C", SeatType.AISLE));  
        seats.add(new Seat("1D", SeatType.AISLE));  
        seats.add(new Seat("1E", SeatType.MIDDLE)); 
        seats.add(new Seat("1F", SeatType.WINDOW)); 
        
        // Let's pretend 1A is already booked on all flights
        seats.get(0).setBooked(true);

        flightSeats.put(flightNumber, seats);
    }

    /**
     * Provisions a structured seating topology based on overall capacity constraints.
     * 
     * @param flightNumber The unique identifier for the targeted flight
     * @param totalSeats   The maximum passenger capacity to model
     */
    public void initializeAircraftLayout(String flightNumber, int totalSeats) {
        List<Seat> seats = new ArrayList<>();
        int rows = totalSeats / 6;
        if (rows == 0 && totalSeats > 0) rows = 1;

        for (int i = 1; i <= rows; i++) {
            seats.add(new Seat(i + "A", SeatType.WINDOW));
            if (seats.size() >= totalSeats) break;
            seats.add(new Seat(i + "B", SeatType.MIDDLE));
            if (seats.size() >= totalSeats) break;
            seats.add(new Seat(i + "C", SeatType.AISLE));
            if (seats.size() >= totalSeats) break;
            seats.add(new Seat(i + "D", SeatType.AISLE));
            if (seats.size() >= totalSeats) break;
            seats.add(new Seat(i + "E", SeatType.MIDDLE));
            if (seats.size() >= totalSeats) break;
            seats.add(new Seat(i + "F", SeatType.WINDOW));
            if (seats.size() >= totalSeats) break;
        }
        flightSeats.put(flightNumber, seats);
    }

    /**
     * Renders an interactive console visualization detailing current seating availability, locks, and finalized bookings.
     * 
     * @param flightNumber The unique flight identifier queried for layout visualization
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
     * Acquires a temporary reservation mutex on a specific seat to prevent simultaneous selection by other threads.
     * 
     * @param flightNumber The unique flight identifier
     * @param seatNumber   The specific seat coordinate requested (e.g., "1A")
     * @return true if the mutex is successfully acquired
     * @throws SeatLockException if the seat is already finalized, currently locked, or non-existent
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
     * Finalizes the state of a previously locked seat, converting it to a permanent booked status post-transaction.
     * 
     * @param flightNumber The associated flight identifier
     * @param seatNumber   The seat coordinate to confirm
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
     * Revokes any temporary locks or bookings on a seat, restoring its state to globally available inventory.
     * 
     * @param flightNumber The associated flight identifier
     * @param seatNumber   The seat coordinate to free
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
     * Evaluates whether a requested seat coordinate exists within the aircraft layout and is currently unbooked.
     * 
     * @param flightNumber The associated flight identifier
     * @param seatNumber   The specific seat coordinate to inspect
     * @return true if the seat exists and is available for selection, false otherwise
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
