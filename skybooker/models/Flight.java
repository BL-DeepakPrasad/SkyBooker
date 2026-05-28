package com.airline.skybooker.models;

import java.time.LocalDateTime;

public class Flight implements Comparable<Flight> {
    private int flightId;
    private String flightNumber;
    private Airline airline;
    private Airport origin;
    private Airport destination;
    private LocalDateTime departureTime;
    private double basePrice;
    private int availableSeats;

    public Flight(int flightId, String flightNumber, Airline airline, Airport origin, Airport destination, double basePrice, int availableSeats) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.basePrice = basePrice;
        this.availableSeats = availableSeats;
        this.departureTime = LocalDateTime.now().plusDays(1);
    }

    public Airline getAirline() { return airline; }
    public Airport getOrigin() { return origin; }
    public Airport getDestination() { return destination; }

    public double getBasePrice() { return basePrice; }
    public int getAvailableSeats() { return availableSeats; }
    public String getFlightNumber() { return flightNumber; }
    public synchronized void decrementSeats() { this.availableSeats--; }
    
    @Override
    public int compareTo(Flight other) {
        return Double.compare(this.basePrice, other.basePrice);
    }

    @Override
    public String toString() {
        return airline.getName() + " " + flightNumber + " | " +
                origin.getIataCode() + " -> " + destination.getIataCode() +
                " | Price: $" + basePrice + " | Seats: " + availableSeats;
    }
}
