package com.airline.skybooker.models;

/**
 * Represents an airline operating within the platform.
 * This class encapsulates the airline's identity, contact information,
 * and operational status.
 */
public class Airline {
    private int airlineId;
    private String name;
    private String iataCode;
    private String icaoCode;
    private String logoUrl;
    private String country;
    private String contactEmail;
    private String contactPhone;
    private boolean isActive;

    /**
     * Constructs a new Airline with the required basic information.
     * The airline is marked as active by default.
     *
     * @param airlineId the unique identifier for the airline
     * @param name      the full name of the airline
     * @param iataCode  the 2-character IATA code
     * @param icaoCode  the 3-character ICAO code
     */
    public Airline(int airlineId, String name, String iataCode, String icaoCode) {
        this.airlineId = airlineId;
        this.name = name;
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.isActive = true;
    }

    /**
     * Gets the unique identifier of the airline.
     *
     * @return the airline ID
     */
    public int getAirlineId() { return airlineId; }

    /**
     * Gets the full name of the airline.
     *
     * @return the airline name
     */
    public String getName() { return name; }

    /**
     * Gets the IATA code of the airline.
     *
     * @return the 2-character IATA code
     */
    public String getIataCode() { return iataCode; }

    /**
     * Gets the ICAO code of the airline.
     *
     * @return the 3-character ICAO code
     */
    public String getIcaoCode() { return icaoCode; }

    /**
     * Sets the URL to the airline's logo image.
     *
     * @param logoUrl the URL string
     */
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    /**
     * Sets the country of origin for the airline.
     *
     * @param country the country name
     */
    public void setCountry(String country) { this.country = country; }

    /**
     * Sets the primary contact details for the airline.
     *
     * @param email the contact email address
     * @param phone the contact phone number
     */
    public void setContactDetails(String email, String phone) {
        this.contactEmail = email;
        this.contactPhone = phone;
    }

    /**
     * Checks if the airline is currently active and operating.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() { return isActive; }

    /**
     * Sets the operational status of the airline.
     *
     * @param active true to mark as active, false to mark as inactive
     */
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return name + " (" + iataCode + ")";
    }
}
