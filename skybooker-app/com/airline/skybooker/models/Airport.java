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
    
    // Module 9 Rubric Fields
    private boolean isActive = true;
    private String terminals = "T1";
    private String facilities = "Basic";
    private String contactDetails = "N/A";

    /**
     * Private constructor for Builder pattern.
     */
    private Airport(Builder builder) {
        this.airportId = builder.airportId;
        this.name = builder.name;
        this.iataCode = builder.iataCode;
        this.icaoCode = builder.icaoCode;
        this.city = builder.city;
        this.country = builder.country;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.timezone = builder.timezone;
        this.isActive = builder.isActive;
        this.terminals = builder.terminals;
        this.facilities = builder.facilities;
        this.contactDetails = builder.contactDetails;
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

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getTerminals() { return terminals; }
    public void setTerminals(String terminals) { this.terminals = terminals; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public String getContactDetails() { return contactDetails; }
    public void setContactDetails(String contactDetails) { this.contactDetails = contactDetails; }

    /**
     * Prints detailed information for passengers (Module 9.2).
     */
    public String getFullDetails() {
        return String.format("Airport: %s (%s)%nCity: %s, %s%nTimezone: %s%nTerminals: %s%nFacilities: %s%nContact: %s%nStatus: %s",
            name, iataCode, city, country, 
            (timezone != null ? timezone : "Not Set"), 
            terminals, facilities, contactDetails, 
            (isActive ? "ACTIVE" : "INACTIVE")
        );
    }

    @Override
    public String toString() {
        return name + ", " + city + " (" + iataCode + ")" + (isActive ? "" : " [INACTIVE]");
    }

    /**
     * Builder class for constructing Airport objects.
     */
    public static class Builder {
        private int airportId;
        private String name;
        private String iataCode;
        private String icaoCode;
        private String city;
        private String country;
        private double latitude;
        private double longitude;
        private String timezone;
        private boolean isActive = true;
        private String terminals = "T1";
        private String facilities = "Basic";
        private String contactDetails = "N/A";

        public Builder setAirportId(int airportId) { this.airportId = airportId; return this; }
        public Builder setName(String name) { this.name = name; return this; }
        public Builder setIataCode(String iataCode) { this.iataCode = iataCode; return this; }
        public Builder setIcaoCode(String icaoCode) { this.icaoCode = icaoCode; return this; }
        public Builder setCity(String city) { this.city = city; return this; }
        public Builder setCountry(String country) { this.country = country; return this; }
        public Builder setCoordinates(double lat, double lon) { this.latitude = lat; this.longitude = lon; return this; }
        public Builder setTimezone(String timezone) { this.timezone = timezone; return this; }
        public Builder setActive(boolean isActive) { this.isActive = isActive; return this; }
        public Builder setTerminals(String terminals) { this.terminals = terminals; return this; }
        public Builder setFacilities(String facilities) { this.facilities = facilities; return this; }
        public Builder setContactDetails(String contactDetails) { this.contactDetails = contactDetails; return this; }

        public Airport build() {
            return new Airport(this);
        }
    }
}
