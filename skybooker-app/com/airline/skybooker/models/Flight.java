package com.airline.skybooker.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.airline.skybooker.enums.FlightStatus;

/**
 * Scheduled trip a plane makes from one airport to another.
 * This is the central piece of data in the system. We use it to keep track of available seats, ticket prices, departure times, and rules for passengers.
 * By implementing 'Comparable', the system can easily sort lists of flights by price from lowest to highest.
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
    private String departureGate;
    private List<String> assignedCrew;

    /**
     * Internal constructor used by the Builder to create a complete Flight object.
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
        this.departureGate = builder.departureGate != null ? builder.departureGate : "TBD";
        this.assignedCrew = new ArrayList<>();
    }

    /**
     * Helper tool for constructing complex Flight objects step-by-step.
     * Flights have many attributes (some required, some optional). A builder prevents the code from having constructors with 15 confusing arguments.
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
        private String departureGate;

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
        
        public Builder setDepartureGate(String gate) {
            this.departureGate = gate;
            return this;
        }

        public Flight build() {
            // Optional: Add pre-flight validation here (e.g. if origin == destination throw exception)
            return new Flight(this);
        }
    }

    public int getFlightId() { return flightId; }
    public Airline getAirline() { return airline; }
    public Airport getOrigin() { return origin; }
    public Airport getDestination() { return destination; }
    public double getBasePrice() { return basePrice; }
    public int getAvailableSeats() { return availableSeats; }
    public int getTotalCapacity() { return totalCapacity; }
    
    /**
     * Calculates what percentage of the plane is currently full.
     * Useful for deciding when to raise ticket prices or apply discounts.
     * 
     * @return the percentage of booked seats
     */
    public double getOccupancyRate() {
        if (totalCapacity == 0) return 0.0;
        int booked = totalCapacity - availableSeats;
        return ((double) booked / totalCapacity) * 100.0;
    }

    public String getFlightNumber() { return flightNumber; }
    public String getAircraftType() { return aircraftType; }
    public FlightStatus getFlightStatus() { return flightStatus; }
    public String getAmenities() { return amenities; }
    public String getDepartureGate() { return departureGate; }
    public LocalDateTime getDepartureTime() { return departureTime; }

    /**
     * Reduces the number of available seats by one.
     * The 'synchronized' keyword ensures that if two people click "Book" at the exact same millisecond, the system won't mistakenly double-book the same seat.
     */
    public synchronized void decrementSeats() { this.availableSeats--; }
    
    // Admin mutators
    public void setFlightStatus(FlightStatus status) { this.flightStatus = status; }
    public void setDepartureTime(LocalDateTime time) { this.departureTime = time; }
    public void setBasePrice(double price) { this.basePrice = price; }
    public void setDepartureGate(String gate) { this.departureGate = gate; }
    
    /**
     * Changes the base ticket price by a certain percentage.
     * 
     * @param percentage amount to shift the price (e.g., 20.0 to increase by 20%, -10.0 to decrease by 10%)
     */
    public void applyDynamicPricing(double percentage) {
        double multiplier = 1.0 + (percentage / 100.0);
        this.basePrice = this.basePrice * multiplier;
    }
    
    // Crew management
    public List<String> getAssignedCrew() { return new ArrayList<>(assignedCrew); }
    public void addCrewMember(String memberName) { this.assignedCrew.add(memberName); }
    public void removeCrewMember(String memberName) { this.assignedCrew.remove(memberName); }
    
    /**
     * Compares this flight's price against another flight's price.
     * Required by the Comparable interface so Java knows how to sort lists of flights automatically.
     *
     * @param other the flight we are comparing against
     * @return negative if this flight is cheaper, positive if more expensive, zero if equal
     */
    @Override
    public int compareTo(Flight other) {
        return Double.compare(this.basePrice, other.basePrice);
    }

    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return airline.getName() + " " + flightNumber + " | " +
                origin.getIataCode() + " -> " + destination.getIataCode() +
                " | Time: " + departureTime.format(formatter) +
                " | Price: INR " + basePrice + " | Seats: " + availableSeats;
    }

    /**
     * Builds a clean text layout of the flight's policies, times, and destinations.
     * Used mainly to show the passenger a summary before they finalize payment.
     *
     * @return organized text block describing the flight rules
     */
    public String getFullDetails() {
        return "\n============================================\n" +
               "           FLIGHT ITINERARY DETAILS          \n" +
               "============================================\n" +
               "Flight: " + airline.getName() + " (" + flightNumber + ")\n" +
               "Origin: " + origin.getName() + " (" + origin.getIataCode() + ")\n" +
               "Destination: " + destination.getName() + " (" + destination.getIataCode() + ")\n" +
               "Departure: " + departureTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
               "Base Fare: INR " + basePrice + "\n" +
               "Aircraft: " + aircraftType + " | Status: " + flightStatus + "\n" +
               "Amenities: " + amenities + "\n" +
               "--------------------------------------------\n" +
               "Baggage Policy:\n  " + baggageRules + "\n" +
               "Cancellation Policy:\n  " + cancellationPolicy + "\n" +
               "============================================";
    }
}
