package com.airline.skybooker.models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Model representing a generated Boarding Pass.
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

    public String generateBarcode() {
        return "||| || ||| | ||| || ||| || ||| | ||||\n" +
               "PNR: " + pnr + " | FLIGHT: " + flightNumber;
    }

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
