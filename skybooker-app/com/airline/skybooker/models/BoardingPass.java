package com.airline.skybooker.models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Digital ticket that a passenger uses to get on the plane.
 * We need this class so we can bundle all the final travel details together—like the assigned seat, gate, and barcode—after a booking is confirmed.
 * It is mostly unchangeable (immutable) because once a boarding pass is issued, the details shouldn't be randomly edited.
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
     * Creates a new boarding pass containing everything the gate agent needs to verify the passenger.
     *
     * @param pnr               the booking reference code (Passenger Name Record)
     * @param passengerName     full name of the traveler
     * @param flightNumber      the ID of the specific flight
     * @param origin            where the flight takes off from
     * @param destination       where the flight lands
     * @param departureTime     when the flight is scheduled to leave
     * @param seatNumber        the physical seat they will sit in
     * @param gate              the airport terminal gate they need to go to
     * @param baggageAllowance  rules about how many bags they can bring
     * @param specialAssistance true if they need extra help, like a wheelchair
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
     * Lays out the boarding pass into a neat, boxy text format that looks like a real ticket printed on paper.
     * Useful for showing the ticket directly in the console.
     *
     * @return the ticket styled as a string block
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
        sb.append("==================================================\n");
        return sb.toString();
    }

    /**
     * Saves the text-formatted boarding pass into a real file on the computer.
     * This mimics the feature where users click "Download Boarding Pass" on an airline website.
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
