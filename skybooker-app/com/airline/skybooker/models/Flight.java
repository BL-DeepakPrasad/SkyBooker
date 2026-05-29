package com.airline.skybooker.models;

import java.time.LocalDateTime;
import com.airline.skybooker.enums.FlightStatus;

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
    private int totalCapacity;
    private String baggageRules;
    private String cancellationPolicy;
    private String aircraftType;
    private FlightStatus flightStatus;
    private String amenities;

    /**
     * Private constructor used by the Builder.
     */
    private Flight(Builder builder) {
        this.flightId = builder.flightId;
        this.flightNumber = builder.flightNumber;
        this.airline = builder.airline;
        this.origin = builder.origin;
        this.destination = builder.destination;
        this.basePrice = builder.basePrice;
        this.availableSeats = builder.availableSeats;
        this.totalCapacity = builder.totalCapacity != 0 ? builder.totalCapacity : builder.availableSeats;
        this.baggageRules = builder.baggageRules;
        this.cancellationPolicy = builder.cancellationPolicy;
        this.aircraftType = builder.aircraftType != null ? builder.aircraftType : "Boeing 737";
        this.flightStatus = builder.flightStatus != null ? builder.flightStatus : FlightStatus.SCHEDULED;
        this.amenities = builder.amenities != null ? builder.amenities : "Standard";
        this.departureTime = builder.departureTime != null ? builder.departureTime : LocalDateTime.now().plusDays(1);
    }

    /**
     * The Builder Pattern implementation for Flight.
     */
    public static class Builder {
        private int flightId;
        private String flightNumber;
        private Airline airline;
        private Airport origin;
        private Airport destination;
        private double basePrice;
        private int availableSeats;
        private int totalCapacity;
        private String baggageRules;
        private String cancellationPolicy;
        private String aircraftType;
        private FlightStatus flightStatus;
        private String amenities;
        private LocalDateTime departureTime;

        public Builder setFlightId(int flightId) {
            this.flightId = flightId;
            return this;
        }

        public Builder setFlightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
            return this;
        }

        public Builder setAirline(Airline airline) {
            this.airline = airline;
            return this;
        }

        public Builder setOrigin(Airport origin) {
            this.origin = origin;
            return this;
        }

        public Builder setDestination(Airport destination) {
            this.destination = destination;
            return this;
        }

        public Builder setBasePrice(double basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public Builder setAvailableSeats(int availableSeats) {
            this.availableSeats = availableSeats;
            this.totalCapacity = availableSeats; // default assumption unless overridden
            return this;
        }

        public Builder setTotalCapacity(int totalCapacity) {
            this.totalCapacity = totalCapacity;
            return this;
        }

        public Builder setBaggageRules(String baggageRules) {
            this.baggageRules = baggageRules;
            return this;
        }

        public Builder setCancellationPolicy(String cancellationPolicy) {
            this.cancellationPolicy = cancellationPolicy;
            return this;
        }

        public Builder setAircraftType(String aircraftType) {
            this.aircraftType = aircraftType;
            return this;
        }

        public Builder setFlightStatus(FlightStatus flightStatus) {
            this.flightStatus = flightStatus;
            return this;
        }

        public Builder setAmenities(String amenities) {
            this.amenities = amenities;
            return this;
        }

        public Builder setDepartureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        public Flight build() {
            // Optional: Add pre-flight validation here (e.g. if origin == destination throw exception)
            return new Flight(this);
        }
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
    public int getTotalCapacity() { return totalCapacity; }
    
    /**
     * Module 8.3: Calculates the current occupancy rate of the flight.
     */
    public double getOccupancyRate() {
        if (totalCapacity == 0) return 0.0;
        int booked = totalCapacity - availableSeats;
        return ((double) booked / totalCapacity) * 100.0;
    }

    /**
     * Gets the alphanumeric flight number.
     *
     * @return the flight number
     */
    public String getFlightNumber() { return flightNumber; }
    public String getAircraftType() { return aircraftType; }
    public FlightStatus getFlightStatus() { return flightStatus; }
    public String getAmenities() { return amenities; }

    /**
     * Decrements the available seat count by one in a thread-safe manner.
     * This method must be synchronized to prevent race conditions during concurrent bookings.
     */
    public synchronized void decrementSeats() { this.availableSeats--; }
    
    // Mutators for Module 8.2 (Admin Edit)
    public void setFlightStatus(FlightStatus status) { this.flightStatus = status; }
    public void setDepartureTime(LocalDateTime time) { this.departureTime = time; }
    public void setBasePrice(double price) { this.basePrice = price; }
    
    /**
     * Module 8.2: Modifies the base fare dynamically based on demand/season.
     * @param percentage Increase or decrease percentage (e.g., 20.0 for +20%)
     */
    public void applyDynamicPricing(double percentage) {
        double multiplier = 1.0 + (percentage / 100.0);
        this.basePrice = this.basePrice * multiplier;
    }
    
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
               "Aircraft: " + aircraftType + " | Status: " + flightStatus + "\n" +
               "Amenities: " + amenities + "\n" +
               "--------------------------------------------\n" +
               "Baggage Policy:\n  " + baggageRules + "\n" +
               "Cancellation Policy:\n  " + cancellationPolicy + "\n" +
               "============================================";
    }
}
