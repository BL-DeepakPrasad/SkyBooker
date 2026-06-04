package com.airline.skybooker.managers;

import com.airline.skybooker.models.Airport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory repository for airport data.
 * Centralizes airport lookups and searches to avoid repeated database hits during flight scheduling and booking flows.
 * Indexes airports by IATA code and city for quick access to alternatives.
 */
public class AirportManager {
    private static AirportManager instance;
    private final Map<String, Airport> airportCache = new ConcurrentHashMap<>();
    private final Map<String, List<Airport>> cityIndex = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(100);

    private AirportManager() {
        initializeMockData();
    }

    /**
     * Provides access to the single, shared AirportManager instance.
     * Ensures all parts of the application read from the same in-memory cache.
     *
     * @return the singleton AirportManager instance
     */
    public static synchronized AirportManager getInstance() {
        if (instance == null) {
            instance = new AirportManager();
        }
        return instance;
    }

    private void initializeMockData() {
        Airport del = new Airport.Builder()
            .setAirportId(idGenerator.incrementAndGet()).setName("Indira Gandhi International").setIataCode("DEL").setCity("New Delhi").setCountry("India")
            .setTimezone("Asia/Kolkata").setTerminals("T1, T2, T3").setFacilities("Lounges, Duty Free, Transit Hotel")
            .setContactDetails("info@newdelhiairport.in").build();
        
        Airport bom = new Airport.Builder()
            .setAirportId(idGenerator.incrementAndGet()).setName("Chhatrapati Shivaji Maharaj").setIataCode("BOM").setCity("Mumbai").setCountry("India")
            .setTimezone("Asia/Kolkata").setTerminals("T1, T2").build();
        
        Airport blr = new Airport.Builder()
            .setAirportId(idGenerator.incrementAndGet()).setName("Kempegowda International").setIataCode("BLR").setCity("Bangalore").setCountry("India")
            .setTimezone("Asia/Kolkata").build();
        
        Airport jfk = new Airport.Builder()
            .setAirportId(idGenerator.incrementAndGet()).setName("John F. Kennedy International").setIataCode("JFK").setCity("New York").setCountry("USA")
            .setTimezone("America/New_York").setTerminals("T1, T4, T5, T7, T8").build();

        addAirport(del);
        addAirport(bom);
        addAirport(blr);
        addAirport(jfk);
    }

    /**
     * Adds an airport to the local cache and search indexes.
     * Keeps the system's airport list up-to-date for flight route planning.
     *
     * @param airport the constructed Airport object to store
     */
    public void addAirport(Airport airport) {
        airportCache.put(airport.getIataCode().toUpperCase(), airport);
        // Add to city index for O(1) alternative airport lookups
        cityIndex.computeIfAbsent(airport.getCity().toLowerCase(), k -> new ArrayList<>()).add(airport);
    }

    /**
     * Validates and saves a newly configured airport into the system.
     * Primarily used by admin tools when the airline expands its operational network to new destinations.
     *
     * @param name           the full official name of the airport
     * @param iata           the unique 3-letter IATA code
     * @param city           the city where the airport is located
     * @param country        the country of the airport
     * @param timezone       the timezone in which the airport operates
     * @param terminals      the available terminals as a comma-separated string
     * @param facilities     the list of facilities provided by the airport
     * @param contactDetails the primary contact email or phone number
     * @throws IllegalArgumentException if the IATA code is already registered
     */
    public void createAirport(String name, String iata, String city, String country, String timezone, String terminals, String facilities, String contactDetails) {
        if (getAirportByCode(iata).isPresent()) {
            throw new IllegalArgumentException("Airport with IATA code " + iata + " already exists.");
        }
        
        Airport newAirport = new Airport.Builder()
            .setAirportId(idGenerator.incrementAndGet())
            .setName(name).setIataCode(iata).setCity(city).setCountry(country)
            .setTimezone(timezone).setTerminals(terminals).setFacilities(facilities)
            .setContactDetails(contactDetails).build();
        addAirport(newAirport);
    }

    /**
     * Updates physical infrastructure details like terminals and facilities for an airport.
     * Allows admins to reflect real-world changes, such as terminal renovations or newly added lounges.
     *
     * @param iata       the 3-letter IATA code of the airport to update
     * @param terminals  the updated list of operational terminals, or null if unchanged
     * @param facilities the updated list of facilities, or null if unchanged
     * @throws IllegalArgumentException if no airport matches the provided IATA code
     */
    public void updateAirport(String iata, String terminals, String facilities) {
        Optional<Airport> opt = getAirportByCode(iata);
        if (opt.isPresent()) {
            Airport airport = opt.get();
            if (terminals != null && !terminals.isEmpty()) airport.setTerminals(terminals);
            if (facilities != null && !facilities.isEmpty()) airport.setFacilities(facilities);
        } else {
            throw new IllegalArgumentException("Airport not found.");
        }
    }

    /**
     * Activates or deactivates an airport for future bookings.
     * Used to temporarily halt operations at a location due to geopolitical events, severe weather, or runway maintenance.
     *
     * @param iata the 3-letter IATA code of the target airport
     * @return true if the airport is now active, false if suspended
     * @throws IllegalArgumentException if no airport matches the provided IATA code
     */
    public boolean toggleAirportStatus(String iata) {
        Optional<Airport> opt = getAirportByCode(iata);
        if (opt.isPresent()) {
            Airport airport = opt.get();
            airport.setActive(!airport.isActive());
            return airport.isActive();
        } else {
            throw new IllegalArgumentException("Airport not found.");
        }
    }

    /**
     * Looks up an airport directly by its IATA code.
     * Used across the system whenever an origin or destination needs to be resolved into full airport details.
     *
     * @param code the exact 3-letter IATA code
     * @return an Optional containing the matched Airport, or empty if not found
     */
    public Optional<Airport> getAirportByCode(String code) {
        return Optional.ofNullable(airportCache.get(code.toUpperCase()));
    }

    /**
     * Returns all registered airports currently loaded in memory.
     * Useful for populating dropdown menus or maps on the frontend.
     *
     * @return a list containing all active and inactive airports
     */
    public List<Airport> getAllAirports() {
        return new ArrayList<>(airportCache.values());
    }

    /**
     * Finds airports matching a user's search string.
     * Powers the autocomplete features in the booking search bar by checking codes, cities, and names.
     *
     * @param query the search string to match against airport fields
     * @return a filtered list of airports fulfilling the search criteria
     */
    public List<Airport> searchAirports(String query) {
        String lowerQuery = query.toLowerCase();
        return airportCache.values().stream()
                .filter(a -> a.getIataCode().toLowerCase().contains(lowerQuery) || 
                             a.getCity().toLowerCase().contains(lowerQuery) || 
                             a.getName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * Finds nearby airports located in the same city.
     * Helps customers find cheaper or more convenient flights when their primary airport choice is unavailable or too expensive.
     *
     * @param iataCode the IATA code of the base airport
     * @return a list of alternative airports in the same city, excluding the base airport
     */
    public List<Airport> getAlternativeAirports(String iataCode) {
        Optional<Airport> opt = getAirportByCode(iataCode);
        if (!opt.isPresent()) return Collections.emptyList();
        
        Airport original = opt.get();
        List<Airport> inCity = cityIndex.getOrDefault(original.getCity().toLowerCase(), Collections.emptyList());
        
        return inCity.stream()
                .filter(a -> !a.getIataCode().equalsIgnoreCase(iataCode))
                .collect(Collectors.toList());
    }
}
