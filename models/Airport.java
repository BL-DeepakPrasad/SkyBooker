package com.airline.skybooker.models;

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

    public Airport(int airportId, String name, String iataCode, String city, String country) {
        this.airportId = airportId;
        this.name = name;
        this.iataCode = iataCode;
        this.city = city;
        this.country = country;
    }

    // Getters and Setters
    public int getAirportId() { return airportId; }
    public String getName() { return name; }
    public String getIataCode() { return iataCode; }
    public String getIcaoCode() { return icaoCode; }
    public void setIcaoCode(String icaoCode) { this.icaoCode = icaoCode; }

    public String getCity() { return city; }
    public String getCountry() { return country; }

    public void setCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    @Override
    public String toString() {
        return name + ", " + city + " (" + iataCode + ")";
    }
}