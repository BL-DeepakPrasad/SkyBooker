package com.airline.skybooker.models;


import java.time.LocalDate;

public class Passenger {
    private int passengerId;

    // CHANGED: Replaced primitive String ID with the actual Booking object
    private Booking booking;

    private String title;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String passportNumber;
    private String nationality;
    private LocalDate passportExpiry;


    private Seat seat;

    private String ticketNumber;
    private String passengerType;


    public Passenger(int passengerId, Booking booking, String title, String firstName, String lastName,
                     LocalDate dateOfBirth, String gender, String passengerType) {
        this.passengerId = passengerId;
        this.booking = booking;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.passengerType = passengerType;
    }

    // Getters
    public int getPassengerId() { return passengerId; }
    public Booking getBooking() { return booking; }
    public String getTitle() { return title; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getPassportNumber() { return passportNumber; }
    public String getNationality() { return nationality; }
    public LocalDate getPassportExpiry() { return passportExpiry; }
    public Seat getSeat() { return seat; }
    public String getTicketNumber() { return ticketNumber; }
    public String getPassengerType() { return passengerType; }


    public void setPassportDetails(String passportNumber, String nationality, LocalDate passportExpiry) {
        this.passportNumber = passportNumber;
        this.nationality = nationality;
        this.passportExpiry = passportExpiry;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }


    @Override
    public String toString() {
        String assignedSeat = (seat != null) ? seat.getSeatNumber() : "Unassigned";
        return getFullName() + " (" + passengerType + ") | Seat: " + assignedSeat;
    }
}