package com.airline.skybooker.models;

import java.time.LocalDateTime;

/**
 * Represents a scheduled flight between an origin and destination airport.
 * This class encapsulates flight details, pricing, and seat availability.
 * It implements {@link Comparable} to enable default sorting by base price.
 */
public class Flight implements Comparable<Flight> {
    private int flightId;
    private String flightNumber;
    private Airline airline;
    private Airport origin;
    private Airport destination;
    private LocalDateTime departureTime;
    private double basePrice;
    private int availableSeats;
    private String baggageRules;
    private String cancellationPolicy;

    /**
     * Constructs a new Flight schedule.
     * The departure time is automatically set to one day from the current time for demonstration purposes.
     *
     * @param flightId       the unique identifier for the flight
     * @param flightNumber   the alphanumeric flight number (e.g., AI-101)
     * @param airline        the airline operating this flight
     * @param origin         the departure airport
     * @param destination    the arrival airport
     * @param basePrice      the base fare price for this flight
     * @param availableSeats the total number of seats currently available for booking
     * @param baggageRules   the baggage policy
     * @param cancellationPolicy the cancellation rules
     */
    public Flight(int flightId, String flightNumber, Airline airline, Airport origin, Airport destination, double basePrice, int availableSeats, String baggageRules, String cancellationPolicy) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.basePrice = basePrice;
        this.availableSeats = availableSeats;
        this.baggageRules = baggageRules;
        this.cancellationPolicy = cancellationPolicy;
        this.departureTime = LocalDateTime.now().plusDays(1);
    }

    /**
     * Gets the unique identifier for the flight.
     *
     * @return the flight ID
     */
    public int getFlightId() { return flightId; }

    /**
     * Gets the airline operating the flight.
     *
     * @return the {@link Airline} instance
     */
    public Airline getAirline() { return airline; }

    /**
     * Gets the origin airport of the flight.
     *
     * @return the {@link Airport} instance
     */
    public Airport getOrigin() { return origin; }

    /**
     * Gets the destination airport of the flight.
     *
     * @return the {@link Airport} instance
     */
    public Airport getDestination() { return destination; }

    /**
     * Gets the base ticket price.
     *
     * @return the base price
     */
    public double getBasePrice() { return basePrice; }

    /**
     * Gets the current count of available seats.
     *
     * @return the number of seats available
     */
    public int getAvailableSeats() { return availableSeats; }

    /**
     * Gets the alphanumeric flight number.
     *
     * @return the flight number
     */
    public String getFlightNumber() { return flightNumber; }

    /**
     * Decrements the available seat count by one in a thread-safe manner.
     * This method must be synchronized to prevent race conditions during concurrent bookings.
     */
    public synchronized void decrementSeats() { this.availableSeats--; }
    
    /**
     * Compares this flight with another flight based on the base price.
     *
     * @param other the other flight to be compared
     * @return a negative integer, zero, or a positive integer as this flight's price
     *         is less than, equal to, or greater than the specified flight's price
     */
    @Override
    public int compareTo(Flight other) {
        return Double.compare(this.basePrice, other.basePrice);
    }

    public String toString() {
        return airline.getName() + " " + flightNumber + " | " +
                origin.getIataCode() + " -> " + destination.getIataCode() +
                " | Price: $" + basePrice + " | Seats: " + availableSeats;
    }

    /**
     * Returns a beautifully formatted string containing the full itinerary,
     * baggage rules, and cancellation policies (UC 4).
     *
     * @return formatted flight details
     */
    public String getFullDetails() {
        return "\n============================================\n" +
               "           FLIGHT ITINERARY DETAILS          \n" +
               "============================================\n" +
               "Flight: " + airline.getName() + " (" + flightNumber + ")\n" +
               "Origin: " + origin.getName() + " (" + origin.getIataCode() + ")\n" +
               "Destination: " + destination.getName() + " (" + destination.getIataCode() + ")\n" +
               "Departure: " + departureTime.toString().replace("T", " ") + "\n" +
               "Base Fare: $" + basePrice + "\n" +
               "--------------------------------------------\n" +
               "Baggage Policy:\n  " + baggageRules + "\n" +
               "Cancellation Policy:\n  " + cancellationPolicy + "\n" +
               "============================================";
    }
}
