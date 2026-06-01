package com.airline.skybooker.managers;

import com.airline.skybooker.models.Airport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * Orchestrator for managing airport geographic data, indexing, and lookup operations.
 * Maintains in-memory caches for fast retrieval by IATA code and city.
 */
public class AirportManager {
    private static AirportManager instance;
    private final Map<String, Airport> airportCache = new ConcurrentHashMap<>();
    private final Map<String, List<Airport>> cityIndex = new ConcurrentHashMap<>();

    private AirportManager() {
        initializeMockData();
    }

    /**
     * Retrieves the singleton instance of the AirportManager.
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
            .setAirportId(1).setName("Indira Gandhi International").setIataCode("DEL").setCity("New Delhi").setCountry("India")
            .setTimezone("Asia/Kolkata").setTerminals("T1, T2, T3").setFacilities("Lounges, Duty Free, Transit Hotel")
            .setContactDetails("info@newdelhiairport.in").build();
        
        Airport bom = new Airport.Builder()
            .setAirportId(2).setName("Chhatrapati Shivaji Maharaj").setIataCode("BOM").setCity("Mumbai").setCountry("India")
            .setTimezone("Asia/Kolkata").setTerminals("T1, T2").build();
        
        Airport blr = new Airport.Builder()
            .setAirportId(3).setName("Kempegowda International").setIataCode("BLR").setCity("Bangalore").setCountry("India")
            .setTimezone("Asia/Kolkata").build();
        
        Airport jfk = new Airport.Builder()
            .setAirportId(4).setName("John F. Kennedy International").setIataCode("JFK").setCity("New York").setCountry("USA")
            .setTimezone("America/New_York").setTerminals("T1, T4, T5, T7, T8").build();

        addAirport(del);
        addAirport(bom);
        addAirport(blr);
        addAirport(jfk);
    }

    /**
     * Registers a new airport instance within the cache and city-based search index.
     *
     * @param airport the constructed Airport object to register
     */
    public void addAirport(Airport airport) {
        airportCache.put(airport.getIataCode().toUpperCase(), airport);
        // Add to city index for O(1) alternative airport lookups
        cityIndex.computeIfAbsent(airport.getCity().toLowerCase(), k -> new ArrayList<>()).add(airport);
    }

    /**
     * Constructs and persists a new airport record using the provided configuration parameters.
     *
     * @param name           the full official name of the airport
     * @param iata           the unique 3-letter IATA code
     * @param city           the city where the airport is located
     * @param country        the country of the airport
     * @param timezone       the timezone in which the airport operates
     * @param terminals      the available terminals as a comma-separated string
     * @param facilities     the list of facilities provided by the airport
     * @param contactDetails the primary contact email or phone number
     */
    public void createAirport(String name, String iata, String city, String country, String timezone, String terminals, String facilities, String contactDetails) {
        Airport newAirport = new Airport.Builder()
            .setAirportId((int)(Math.random() * 10000))
            .setName(name).setIataCode(iata).setCity(city).setCountry(country)
            .setTimezone(timezone).setTerminals(terminals).setFacilities(facilities)
            .setContactDetails(contactDetails).build();
        addAirport(newAirport);
    }

    /**
     * Modifies the operational terminals and facilities for an existing airport identified by its IATA code.
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
     * Switches the active operational state of an airport based on its IATA code.
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
     * Retrieves an airport record based on its unique IATA identifier.
     *
     * @param code the exact 3-letter IATA code
     * @return an Optional containing the matched Airport, or empty if not found
     */
    public Optional<Airport> getAirportByCode(String code) {
        return Optional.ofNullable(airportCache.get(code.toUpperCase()));
    }

    /**
     * Fetches the complete collection of registered airports in the system.
     *
     * @return a newly constructed list containing all active and inactive airports
     */
    public List<Airport> getAllAirports() {
        return new ArrayList<>(airportCache.values());
    }

    /**
     * Scans the airport registry for matches against IATA code, city, or airport name.
     * Case-insensitive partial matching is applied.
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
     * Recommends nearby alternative airports operating within the same metropolitan area.
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
