package com.airline.skybooker.models;

/**
 * Physical location where flights take off and land.
 * We need this class so flights have specific origins and destinations, allowing the system to calculate routes and display location details to passengers.
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
    
    // Airport facilities
    private boolean isActive = true;
    private String terminals = "T1";
    private String facilities = "Basic";
    private String contactDetails = "N/A";

    /**
     * Internal constructor used by the Builder to create an Airport object.
     * We use a builder pattern here because airports have many optional properties (like facilities or multiple terminals), making a normal constructor too messy.
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

    public int getAirportId() { return airportId; }
    public String getName() { return name; }
    public String getIataCode() { return iataCode; }
    public String getIcaoCode() { return icaoCode; }
    public void setIcaoCode(String icaoCode) { this.icaoCode = icaoCode; }
    public String getCity() { return city; }
    public String getCountry() { return country; }

    /**
     * Updates the exact map coordinates of the airport.
     * Helpful if we want to show a map view or calculate actual flight distances later.
     *
     * @param latitude  the latitude coordinate
     * @param longitude the longitude coordinate
     */
    public void setCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTimezone() { return timezone; }
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
     * Combines all important airport information into a single, easy-to-read block of text.
     * Used mainly to show detailed airport info to an admin or a curious passenger.
     *
     * @return formatted text block with the airport's location, status, and amenities
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
     * Helper tool to construct Airport objects step-by-step.
     * Allows setting only the fields you know about without worrying about the order of arguments.
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
