package com.airline.skybooker.models;

/**
 * Company that actually owns and operates the flights (like Delta or Emirates).
 * We need this class to group flights by their airline and easily display the airline's logo or contact info to passengers when they book.
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
     * Creates a new airline record in the system.
     * New airlines are marked as active right away so they can immediately start offering flights.
     *
     * @param airlineId unique database ID for the airline
     * @param name      full company name (e.g., "Emirates Airlines")
     * @param iataCode  the 2-letter code you see on tickets (like "EK")
     * @param icaoCode  the 3-letter code used by air traffic control
     */
    public Airline(int airlineId, String name, String iataCode, String icaoCode) {
        this.airlineId = airlineId;
        this.name = name;
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.isActive = true;
    }

    public int getAirlineId() { return airlineId; }
    public String getName() { return name; }
    public String getIataCode() { return iataCode; }
    public String getIcaoCode() { return icaoCode; }

    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public void setCountry(String country) { this.country = country; }

    /**
     * Updates both email and phone at once so we don't end up with mismatched contact info if only one setter gets called.
     *
     * @param email customer support email
     * @param phone customer support phone number
     */
    public void setContactDetails(String email, String phone) {
        this.contactEmail = email;
        this.contactPhone = phone;
    }

    public boolean isActive() { return isActive; }

    /**
     * Turns an airline's operations on or off. 
     * Useful if an airline goes out of business or gets suspended, so we don't have to delete their historical data from the database.
     *
     * @param active true if the airline is currently operating, false otherwise
     */
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return name + " (" + iataCode + ")";
    }
}
