package com.airline.skybooker.models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Immutable ticket artifact for a confirmed passenger flight.
 * Encapsulates necessary travel details required for flight boarding and gate validation.
 */
public class BoardingPass {

    private final String pnr;
    private final String passengerName;
    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final String departureTime;
    private final String seatNumber;
    private final String gate;
    private final String baggageAllowance;
    private final boolean specialAssistance;

    /**
     * Constructs a boarding pass with complete passenger and flight itinerary details.
     *
     * @param pnr               Passenger Name Record identifying the booking
     * @param passengerName     full name of the traveling passenger
     * @param flightNumber      alphanumeric identifier of the scheduled flight
     * @param origin            departure airport name or code
     * @param destination       arrival airport name or code
     * @param departureTime     formatted string of the scheduled departure time
     * @param seatNumber        assigned physical seat within the aircraft
     * @param gate              departure terminal gate assignment
     * @param baggageAllowance  permitted baggage limit (e.g., "15kg Cabin, 25kg Check-in")
     * @param specialAssistance true if the passenger requires wheelchair or medical support
     */
    public BoardingPass(String pnr, String passengerName, String flightNumber, String origin, 
                        String destination, String departureTime, String seatNumber, String gate,
                        String baggageAllowance, boolean specialAssistance) {
        this.pnr = pnr;
        this.passengerName = passengerName;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.seatNumber = seatNumber;
        this.gate = gate;
        this.baggageAllowance = baggageAllowance;
        this.specialAssistance = specialAssistance;
    }

    /**
     * Generates a simulated ASCII barcode representation for scanning purposes.
     * Embeds the PNR and flight number for automated validation.
     *
     * @return multiline string representing a scannable barcode layout
     */
    public String generateBarcode() {
        return "||| || ||| | ||| || ||| || ||| | ||||\n" +
               "PNR: " + pnr + " | FLIGHT: " + flightNumber;
    }

    /**
     * Composes the full boarding pass layout into a printable ASCII format.
     * Organizes itinerary details, seat assignment, and barcode into a structured ticket.
     *
     * @return the formatted boarding pass string
     */
    public String getFormattedPass() {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("                 BOARDING PASS                    \n");
        sb.append("==================================================\n");
        sb.append(String.format("PASSENGER: %-20s PNR: %s%n", passengerName, pnr));
        sb.append(String.format("FLIGHT: %-23s GATE: %s%n", flightNumber, gate));
        sb.append(String.format("FROM: %-25s TO: %s%n", origin, destination));
        sb.append(String.format("DEPARTURE: %-20s SEAT: %s%n", departureTime, seatNumber));
        sb.append(String.format("BAGGAGE: %-22s ASSISTANCE: %s%n", baggageAllowance, specialAssistance ? "Yes" : "No"));
        sb.append("--------------------------------------------------\n");
        sb.append(generateBarcode()).append("\n");
        sb.append("==================================================\n");
        return sb.toString();
    }

    /**
     * Exports the formatted boarding pass to a local text file.
     * Uses the PNR code as the filename prefix for easy retrieval.
     */
    public void downloadToFile() {
        String filename = pnr + "_BoardingPass.txt";
        try (FileWriter writer = new FileWriter(new File(filename))) {
            writer.write(getFormattedPass());
            System.out.println("[DOWNLOAD] Boarding pass saved to: " + filename);
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to download boarding pass: " + e.getMessage());
        }
    }
}
