package com.airline.skybooker.services;

import com.airline.skybooker.exception.SeatLockException;
import com.airline.skybooker.models.Seat;
import com.airline.skybooker.enums.SeatType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer responsible for seat operations.
 * Designed to mirror a Spring Boot @Service. Handles concurrent locking.
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
     * Displays a text-based grid map of the seats for a specific flight.
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
     * Attempts to lock a seat for a user.
     * Uses synchronized blocks to prevent concurrent thread race conditions.
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
}
