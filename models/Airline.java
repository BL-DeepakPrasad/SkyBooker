package com.airline.skybooker.models;


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

    public Airline(int airlineId, String name, String iataCode, String icaoCode) {
        this.airlineId = airlineId;
        this.name = name;
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.isActive = true; // Default state
    }

    // Getters and Setters
    public int getAirlineId() { return airlineId; }
    public String getName() { return name; }
    public String getIataCode() { return iataCode; }
    public String getIcaoCode() { return icaoCode; }

    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public void setCountry(String country) { this.country = country; }
    public void setContactDetails(String email, String phone) {
        this.contactEmail = email;
        this.contactPhone = phone;
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return name + " (" + iataCode + ")";
    }
}