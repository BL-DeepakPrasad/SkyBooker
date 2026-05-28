package com.airline.skybooker.managers;

import com.airline.skybooker.exception.FlightNotFoundException;
import com.airline.skybooker.interfaces.Searchable;
import com.airline.skybooker.models.Airline;
import com.airline.skybooker.models.Airport;
import com.airline.skybooker.models.Flight;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;


public class FlightManager implements Searchable {
    private static volatile FlightManager instance;
    private List<Flight> flightDatabase;
    private Map<String, List<Flight>> searchCache;
    private Map<String, List<Flight>> routeIndex;

    private FlightManager() {
        this.flightDatabase = new ArrayList<>();

        Airline airIndia = new Airline(100, "Air India", "AI", "AIC");
        Airline indigo = new Airline(101, "IndiGo", "6E", "IGO");

        Airport del = new Airport(1, "Indira Gandhi Int", "DEL", "New Delhi", "India");
        Airport bom = new Airport(2, "Chhatrapati Shivaji", "BOM", "Mumbai", "India");
        Airport blr = new Airport(3, "Kempegowda Int", "BLR", "Bengaluru", "India");

        flightDatabase.add(new Flight(1, "AI-101", airIndia, del, bom, 120.50, 50));
        flightDatabase.add(new Flight(2, "IG-202", indigo, del, bom, 95.00, 10));
        flightDatabase.add(new Flight(3, "AI-303", airIndia, bom, blr, 150.00, 5));

        this.searchCache = new ConcurrentHashMap<>();
        this.routeIndex = flightDatabase.stream().collect(
            Collectors.groupingBy(f -> f.getOrigin().getIataCode().toUpperCase() + "-" + f.getDestination().getIataCode().toUpperCase())
        );
    }

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

    @Override
    public List<Flight> getFlightsByAirline(int airlineId) {
        Map<Integer, List<Flight>> groupedFlights =
                flightDatabase
                        .stream()
                        .collect(Collectors
                                .groupingBy(f -> f.getAirline().getAirlineId()));

        return groupedFlights.getOrDefault(airlineId, Collections.emptyList());
    }

    public double getAverageFare(String originCode, String destinationCode) {
        String routeKey = originCode.toUpperCase() + "-" + destinationCode.toUpperCase();
        List<Flight> indexedFlights = routeIndex.getOrDefault(routeKey, Collections.emptyList());
        return indexedFlights.stream()
                .collect(Collectors.averagingDouble(Flight::getBasePrice));
    }

    public Optional<Flight> getCheapestFlight(String originCode, String destinationCode) {
        String routeKey = originCode.toUpperCase() + "-" + destinationCode.toUpperCase();
        List<Flight> indexedFlights = routeIndex.getOrDefault(routeKey, Collections.emptyList());
        return indexedFlights.stream()
                .filter(f -> f.getAvailableSeats() > 0)
                .collect(Collectors.minBy(Comparator.comparingDouble(Flight::getBasePrice)));
    }
}
