package com.airline.skybooker.managers;

import com.airline.skybooker.models.Airport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Singleton repository for managing Airport geographic data.
 */
public class AirportManager {
    private static AirportManager instance;
    private final Map<String, Airport> airportCache = new ConcurrentHashMap<>();

    private AirportManager() {
        initializeMockData();
    }

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
     * Adds a new airport to the system.
     */
    public void addAirport(Airport airport) {
        airportCache.put(airport.getIataCode().toUpperCase(), airport);
    }

    /**
     * Service method to create and register an airport.
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
     * Service method to update an existing airport.
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
     * Service method to toggle airport active status.
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
     * Retrieves an airport by exact IATA code.
     */
    public Optional<Airport> getAirportByCode(String code) {
        return Optional.ofNullable(airportCache.get(code.toUpperCase()));
    }

    /**
     * Gets all registered airports.
     */
    public List<Airport> getAllAirports() {
        return new ArrayList<>(airportCache.values());
    }

    /**
     * Searches by IATA code, City name, or Airport name.
     */
    public List<Airport> searchAirports(String query) {
        String lowerQuery = query.toLowerCase();
        return airportCache.values().stream()
                .filter(a -> a.getIataCode().toLowerCase().contains(lowerQuery) || 
                             a.getCity().toLowerCase().contains(lowerQuery) || 
                             a.getName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }
}
