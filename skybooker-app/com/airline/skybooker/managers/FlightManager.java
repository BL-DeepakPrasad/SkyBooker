package com.airline.skybooker.managers;

import com.airline.skybooker.exception.FlightNotFoundException;
import com.airline.skybooker.interfaces.Searchable;
import com.airline.skybooker.models.Airline;
import com.airline.skybooker.models.Airport;
import com.airline.skybooker.models.Flight;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

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

        Airline airIndia = new Airline(100, "Air India", "AI", "AIC");
        Airline indigo = new Airline(101, "IndiGo", "6E", "IGO");

        Airport del = new Airport(1, "Indira Gandhi Int", "DEL", "New Delhi", "India");
        Airport bom = new Airport(2, "Chhatrapati Shivaji", "BOM", "Mumbai", "India");
        Airport blr = new Airport(3, "Kempegowda Int", "BLR", "Bengaluru", "India");

        flightDatabase.add(new Flight.Builder()
                .setFlightId(1)
                .setFlightNumber("AI-101")
                .setAirline(airIndia)
                .setOrigin(del)
                .setDestination(bom)
                .setBasePrice(120.50)
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
                    .filter(f -> f.getAvailableSeats() > 0)
                    .collect(Collectors.toList());
            if (cachedResults.isEmpty()) {
                throw new FlightNotFoundException(originCode, destinationCode);
            }
            return cachedResults;
        }

        List<Flight> indexedFlights = routeIndex.getOrDefault(cacheKey, Collections.emptyList());

        List<Flight> results = indexedFlights
                .stream()
                .filter(f -> f.getAvailableSeats() > 0)
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
     * Retrieves a specific flight by its flight number.
     *
     * @param flightNumber the flight number (e.g., AI-101)
     * @return an Optional containing the flight if found
     */
    public Optional<Flight> getFlightByNumber(String flightNumber) {
        return flightDatabase.stream()
                .filter(f -> f.getFlightNumber().equalsIgnoreCase(flightNumber))
                .findFirst();
    }
}
