package com.airline.skybooker.managers;

import com.airline.skybooker.exception.FlightNotFoundException;
import com.airline.skybooker.interfaces.Searchable;
import com.airline.skybooker.models.Airline;
import com.airline.skybooker.models.Airport;
import com.airline.skybooker.models.Flight;
import com.airline.skybooker.services.SeatService;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;
import com.airline.skybooker.managers.NotificationManager;
import com.airline.skybooker.managers.AuthenticationManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import com.airline.skybooker.enums.FlightStatus;
import com.airline.skybooker.managers.AirportManager;

/**
 * The FlightManager acts as a centralized service for managing flight inventories,
 * search operations, and route indexing.
 * <p>
 * It follows the Singleton design pattern to ensure a single instance is utilized
 * across the application. It heavily utilizes the Java Streams API for data manipulation
 * and ConcurrentHashMap for thread-safe caching.
 * </p>
 */
public class FlightManager implements Searchable {

    /**
     * The volatile singleton instance ensuring thread-safe publication.
     */
    private static volatile FlightManager instance;

    /**
     * The primary database mimicking persistent storage of all scheduled flights.
     */
    private List<Flight> flightDatabase;

    /**
     * An in-memory cache to store frequent search queries for improved performance.
     */
    private Map<String, List<Flight>> searchCache;

    /**
     * A highly optimized index grouping flights by their route (Origin-Destination)
     * for O(1) lookup speeds prior to filtering.
     */
    private Map<String, List<Flight>> routeIndex;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the mock database, routing indexes, and caching layers.
     */
    private FlightManager() {
        this.flightDatabase = new ArrayList<>();
        initializeMockData();
        
        this.searchCache = new ConcurrentHashMap<>();
        this.routeIndex = flightDatabase.stream().collect(
            Collectors.groupingBy(f -> f.getOrigin().getIataCode().toUpperCase() + "-" + f.getDestination().getIataCode().toUpperCase())
        );
    }

    private void initializeMockData() {
        Airline airIndia = new Airline(100, "Air India", "AI", "AIC");
        Airline indigo = new Airline(101, "IndiGo", "6E", "IGO");

        AirportManager am = AirportManager.getInstance();
        Airport del = am.getAirportByCode("DEL").orElse(new Airport.Builder().setAirportId(1).setName("Fallback").setIataCode("DEL").setCity("City").setCountry("Country").build());
        Airport bom = am.getAirportByCode("BOM").orElse(new Airport.Builder().setAirportId(2).setName("Fallback").setIataCode("BOM").setCity("City").setCountry("Country").build());
        Airport blr = am.getAirportByCode("BLR").orElse(new Airport.Builder().setAirportId(3).setName("Fallback").setIataCode("BLR").setCity("City").setCountry("Country").build());

        flightDatabase.add(new Flight.Builder()
                .setFlightId(1)
                .setFlightNumber("AI-101")
                .setAirline(airIndia)
                .setOrigin(del)
                .setDestination(bom)
                .setBasePrice(120.50)
                .setTotalCapacity(60)
                .setAvailableSeats(50)
                .setBaggageRules("1 Cabin (7kg), 1 Checked (15kg)")
                .setCancellationPolicy("Free cancellation up to 24 hrs before departure.")
                .build());

        flightDatabase.add(new Flight.Builder()
                .setFlightId(2)
                .setFlightNumber("IG-202")
                .setAirline(indigo)
                .setOrigin(del)
                .setDestination(bom)
                .setBasePrice(95.00)
                .setTotalCapacity(60)
                .setAvailableSeats(10)
                .setBaggageRules("1 Cabin (7kg) only. Checked bag extra.")
                .setCancellationPolicy("Non-refundable. Date change fee applies.")
                .build());

        flightDatabase.add(new Flight.Builder()
                .setFlightId(3)
                .setFlightNumber("AI-303")
                .setAirline(airIndia)
                .setOrigin(bom)
                .setDestination(blr)
                .setBasePrice(150.00)
                .setTotalCapacity(60)
                .setAvailableSeats(5)
                .setBaggageRules("1 Cabin (7kg), 2 Checked (20kg total)")
                .setCancellationPolicy("Free cancellation up to 48 hrs before departure.")
                .build());

        this.searchCache = new ConcurrentHashMap<>();
        this.routeIndex = flightDatabase.stream().collect(
            Collectors.groupingBy(f -> f.getOrigin().getIataCode().toUpperCase() + "-" + f.getDestination().getIataCode().toUpperCase())
        );
    }

    /**
     * Retrieves the globally unique instance of the FlightManager.
     * Utilizes double-checked locking for thread safety and performance optimization.
     *
     * @return the {@link FlightManager} instance
     */
    public static FlightManager getInstance() {
        if (instance == null) {
            synchronized (FlightManager.class) {
                if (instance == null) {
                    instance = new FlightManager(); 
                }
            }
        }
        return instance;
    }

    /**
     * Searches for available one-way flights between two designated airports.
     * This method utilizes caching to bypass expensive stream operations on identical queries.
     * If the cache misses, it queries the route index, filters by availability,
     * and sorts the result by base price.
     *
     * @param originCode      the IATA code of the departure airport
     * @param destinationCode the IATA code of the arrival airport
     * @return an ordered list of {@link Flight} objects sorted by price
     * @throws FlightNotFoundException if no flights exist or all seats are booked
     */
    @Override
    public List<Flight> searchFlights(String originCode, String destinationCode) {
        String cacheKey = originCode.toUpperCase() + "-" + destinationCode.toUpperCase();
        
        if (searchCache.containsKey(cacheKey)) {
            System.out.println("[CACHE HIT] Returning results from cache.");
            List<Flight> cachedResults = searchCache.get(cacheKey).stream()
                    .filter(f -> f.getAvailableSeats() > 0 && f.getFlightStatus() != com.airline.skybooker.enums.FlightStatus.CANCELLED)
                    .collect(Collectors.toList());
            if (cachedResults.isEmpty()) {
                throw new FlightNotFoundException(originCode, destinationCode);
            }
            return cachedResults;
        }

        List<Flight> indexedFlights = routeIndex.getOrDefault(cacheKey, Collections.emptyList());

        List<Flight> results = indexedFlights
                .stream()
                .filter(f -> f.getAvailableSeats() > 0 && f.getFlightStatus() != com.airline.skybooker.enums.FlightStatus.CANCELLED)
                .sorted(Comparator.comparingDouble(Flight::getBasePrice))
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            throw new FlightNotFoundException(originCode, destinationCode);
        }
        
        searchCache.put(cacheKey, results);
        return results;
    }

    /**
     * Groups and retrieves all scheduled flights managed by a specific airline.
     *
     * @param airlineId the unique identifier of the target airline
     * @return a list of associated {@link Flight} instances
     */
    @Override
    public List<Flight> getFlightsByAirline(int airlineId) {
        Map<Integer, List<Flight>> groupedFlights =
                flightDatabase
                        .stream()
                        .collect(Collectors
                                .groupingBy(f -> f.getAirline().getAirlineId()));

        return groupedFlights.getOrDefault(airlineId, Collections.emptyList());
    }

    /**
     * Calculates the mean base fare for a given route using Stream aggregations.
     *
     * @param originCode      the departure airport IATA code
     * @param destinationCode the arrival airport IATA code
     * @return the average base price, or 0.0 if the route does not exist
     */
    public double getAverageFare(String originCode, String destinationCode) {
        String routeKey = originCode.toUpperCase() + "-" + destinationCode.toUpperCase();
        List<Flight> indexedFlights = routeIndex.getOrDefault(routeKey, Collections.emptyList());
        return indexedFlights.stream()
                .collect(Collectors.averagingDouble(Flight::getBasePrice));
    }

    /**
     * Identifies the absolute most economical active flight for a route.
     *
     * @param originCode      the departure airport IATA code
     * @param destinationCode the arrival airport IATA code
     * @return an {@link Optional} containing the cheapest Flight, or empty if none exist
     */
    public Optional<Flight> getCheapestFlight(String originCode, String destinationCode) {
        String routeKey = originCode.toUpperCase() + "-" + destinationCode.toUpperCase();
        List<Flight> indexedFlights = routeIndex.getOrDefault(routeKey, Collections.emptyList());
        return indexedFlights.stream()
                .filter(f -> f.getAvailableSeats() > 0)
                .collect(Collectors.minBy(Comparator.comparingDouble(Flight::getBasePrice)));
    }

    /**
     * Retrieves a specific flight based on its designated alphanumeric flight number.
     *
     * @param flightNumber the flight code string (e.g., "AI-101")
     * @return an Optional containing the matched Flight, or empty if not found
     */
    public Optional<Flight> getFlightByNumber(String flightNumber) {
        return flightDatabase.stream()
                .filter(f -> f.getFlightNumber().equalsIgnoreCase(flightNumber))
                .findFirst();
    }

    /**
     * Fetches a flight using its internal unique numeric identifier.
     *
     * @param flightId the primary key identifying the flight
     * @return an Optional containing the corresponding Flight, or empty if no match exists
     */
    public Optional<Flight> getFlightById(int flightId) {
        return flightDatabase.stream()
                .filter(f -> f.getFlightId() == flightId)
                .findFirst();
    }

    /**
     * Registers a new dynamically created flight into the system.
     * Updates the route index and clears the cache to ensure consistency.
     */
    public synchronized void addFlight(Flight flight) {
        flightDatabase.add(flight);
        
        // Update Route Index
        String cacheKey = flight.getOrigin().getIataCode().toUpperCase() + "-" + flight.getDestination().getIataCode().toUpperCase();
        routeIndex.computeIfAbsent(cacheKey, k -> new ArrayList<>()).add(flight);
        
        // Clear Cache to prevent stale search results
        searchCache.clear();
        System.out.println("[DB] Flight " + flight.getFlightNumber() + " successfully inserted.");
    }

    /**
     * Fetches a full snapshot of the current flight database.
     *
     * @return a list containing all scheduled flights
     */
    public List<Flight> getAllFlights() {
        return new ArrayList<>(flightDatabase);
    }

    /**
     * Validates parameters and synthesizes a new Flight schedule, including route setup and seat layout initialization.
     *
     * @param airlineName  the name of the operating airline
     * @param airlineCode  the short airline IATA designator
     * @param flightNumber the designated flight number
     * @param aircraftType the model of the aircraft serving the route
     * @param originCode   the IATA code of the departure airport
     * @param destCode     the IATA code of the arrival airport
     * @param capacity     the maximum passenger capacity for the flight
     * @param baseFare     the starting price point for a standard seat
     * @param baggage      the allowed baggage configuration text
     * @param cancelPolicy the governing cancellation rules text
     * @param amenities    the list of in-flight services provided
     * @param seatService  the service handling the initial seat map generation
     * @throws IllegalArgumentException if the origin or destination airport codes are invalid
     */
    public void createFlight(String airlineName, String airlineCode, String flightNumber, String aircraftType, 
                             String originCode, String destCode, int capacity, double baseFare, 
                             String baggage, String cancelPolicy, String amenities, 
                             SeatService seatService) {
        
        AirportManager am = AirportManager.getInstance();
        Airport origin = am.getAirportByCode(originCode).orElseThrow(() -> new IllegalArgumentException("Origin Airport not found"));
        Airport dest = am.getAirportByCode(destCode).orElseThrow(() -> new IllegalArgumentException("Destination Airport not found"));
        
        Airline airline = new Airline((int)(Math.random() * 10000), airlineName, airlineCode, airlineCode + "C");

        Flight newFlight = new Flight.Builder()
                .setFlightId((int) (Math.random() * 10000))
                .setFlightNumber(flightNumber)
                .setAirline(airline)
                .setOrigin(origin)
                .setDestination(dest)
                .setBasePrice(baseFare)
                .setAvailableSeats(capacity)
                .setTotalCapacity(capacity)
                .setBaggageRules(baggage)
                .setCancellationPolicy(cancelPolicy)
                .setAircraftType(aircraftType)
                .setFlightStatus(FlightStatus.SCHEDULED)
                .setAmenities(amenities)
                .setDepartureTime(LocalDateTime.now().plusDays(7))
                .build();
                
        addFlight(newFlight);
        
        // Dynamically generate the seat map internally in the service layer
        seatService.initializeAircraftLayout(flightNumber, capacity);
    }

    /**
     * Delays or reschedules a flight by adjusting its departure date relative to the current time.
     *
     * @param flightNum the flight identifier
     * @param daysDelay the number of days to offset the departure from today
     * @throws IllegalArgumentException if the flight does not exist
     */
    public void updateFlightDeparture(String flightNum, int daysDelay) {
        Flight f = getFlightByNumber(flightNum).orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        f.setDepartureTime(LocalDateTime.now().plusDays(daysDelay));
        clearCache();
    }

    /**
     * Adjusts the baseline fare price for an existing scheduled flight.
     *
     * @param flightNum the flight identifier
     * @param newFare   the updated base fare amount
     * @throws IllegalArgumentException if the flight does not exist
     */
    public void updateFlightFare(String flightNum, double newFare) {
        Flight f = getFlightByNumber(flightNum).orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        f.setBasePrice(newFare);
        clearCache();
    }

    /**
     * Modifies the base price of a flight proportionally according to demand algorithms.
     *
     * @param flightNum  the flight identifier
     * @param percentage the percentage multiplier to apply (e.g., 10.0 for a 10% increase)
     * @throws IllegalArgumentException if the flight does not exist
     */
    public void applyDynamicPricing(String flightNum, double percentage) {
        Flight f = getFlightByNumber(flightNum).orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        f.applyDynamicPricing(percentage);
        clearCache();
    }

    /**
     * Transitions a flight's operational status and orchestrates alert broadcasts for severe disruptions.
     *
     * @param flightNum the flight identifier
     * @param status    the new operational state (e.g., DELAYED, CANCELLED)
     * @throws IllegalArgumentException if the flight does not exist
     */
    public void updateFlightStatus(String flightNum, FlightStatus status) {
        Flight f = getFlightByNumber(flightNum).orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        f.setFlightStatus(status);
        clearCache();
        
        if (status == FlightStatus.DELAYED || status == FlightStatus.CANCELLED) {
            System.out.println("\n[FLIGHT MANAGER] Broadcast Notification triggered for flight: " + flightNum);
            
            // In a real system, we would fetch all bookings for this flight and notify.
            // For now, we mock broadcast to the currently logged in user if they are a passenger.
            User currentUser = AuthenticationManager.getInstance().getCurrentUser().orElse(null);
            if (currentUser instanceof Passenger) {
                NotificationManager.getInstance().sendFlightAlert(currentUser, f, status.name(), "Please check dashboard for updates.");
            }
        }
    }

    /**
     * Changes the assigned departure terminal gate and alerts impacted passengers.
     *
     * @param flightNum the flight identifier
     * @param newGate   the new gate string identifier (e.g., "G2B")
     * @throws IllegalArgumentException if the flight does not exist
     */
    public void updateFlightGate(String flightNum, String newGate) {
        Flight f = getFlightByNumber(flightNum).orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        f.setDepartureGate(newGate);
        clearCache();
        
        System.out.println("\n[FLIGHT MANAGER] Broadcast Notification triggered for GATE CHANGE: " + flightNum);
        
        // Mocking broadcast to current user
        User currentUser = AuthenticationManager.getInstance().getCurrentUser().orElse(null);
        if (currentUser instanceof Passenger) {
            NotificationManager.getInstance().sendFlightAlert(currentUser, f, "GATE CHANGED", "New Gate is: " + newGate);
        }
    }

    /**
     * Filters the flight database applying predicates for airline, route, and status sequentially.
     *
     * @param airlineCode an optional airline code to filter by, or empty to ignore
     * @param route       an optional route pattern to match, or empty to ignore
     * @param statusStr   an optional status string to enforce, or empty to ignore
     * @return a filtered list of matching flights
     */
    public List<Flight> getFilteredFlights(String airlineCode, String route, String statusStr) {
        return flightDatabase.stream()
            .filter(f -> airlineCode.isEmpty() || f.getAirline().getIataCode().equalsIgnoreCase(airlineCode))
            .filter(f -> {
                if (route.isEmpty()) return true;
                String fRoute = f.getOrigin().getIataCode() + "-" + f.getDestination().getIataCode();
                return fRoute.equalsIgnoreCase(route);
            })
            .filter(f -> statusStr.isEmpty() || f.getFlightStatus().name().equalsIgnoreCase(statusStr))
            .collect(Collectors.toList());
    }

    /**
     * Clears the search cache.
     */
    public synchronized void clearCache() {
        searchCache.clear();
    }
}
