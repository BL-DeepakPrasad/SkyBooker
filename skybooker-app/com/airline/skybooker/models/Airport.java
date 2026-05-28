package com.airline.skybooker.models;

/**
 * Represents a geographical airport within the system.
 * This class encapsulates the airport's location data and standard aviation codes.
 */
public class Airport {
    private int airportId;
    private String name;
    private String iataCode;
    private String icaoCode;
    private String city;
    private String country;
    private double latitude;
    private double longitude;
    private String timezone;

    /**
     * Constructs a new Airport instance.
     *
     * @param airportId the unique identifier for the airport
     * @param name      the full name of the airport
     * @param iataCode  the 3-character IATA location identifier
     * @param city      the city served by the airport
     * @param country   the country where the airport is located
     */
    public Airport(int airportId, String name, String iataCode, String city, String country) {
        this.airportId = airportId;
        this.name = name;
        this.iataCode = iataCode;
        this.city = city;
        this.country = country;
    }

    /**
     * Gets the unique identifier of the airport.
     *
     * @return the airport ID
     */
    public int getAirportId() { return airportId; }

    /**
     * Gets the full name of the airport.
     *
     * @return the airport name
     */
    public String getName() { return name; }

    /**
     * Gets the IATA code of the airport.
     *
     * @return the 3-character IATA code
     */
    public String getIataCode() { return iataCode; }

    /**
     * Gets the ICAO code of the airport.
     *
     * @return the 4-character ICAO code
     */
    public String getIcaoCode() { return icaoCode; }

    /**
     * Sets the ICAO code for the airport.
     *
     * @param icaoCode the 4-character ICAO code
     */
    public void setIcaoCode(String icaoCode) { this.icaoCode = icaoCode; }

    /**
     * Gets the city served by the airport.
     *
     * @return the city name
     */
    public String getCity() { return city; }

    /**
     * Gets the country where the airport is located.
     *
     * @return the country name
     */
    public String getCountry() { return country; }

    /**
     * Sets the geographical coordinates of the airport.
     *
     * @param latitude  the latitude coordinate
     * @param longitude the longitude coordinate
     */
    public void setCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Gets the timezone of the airport.
     *
     * @return the timezone identifier
     */
    public String getTimezone() { return timezone; }

    /**
     * Sets the timezone of the airport.
     *
     * @param timezone the timezone identifier
     */
    public void setTimezone(String timezone) { this.timezone = timezone; }

    @Override
    public String toString() {
        return name + ", " + city + " (" + iataCode + ")";
    }
}
