package com.airline.skybooker.ui;

import com.airline.skybooker.managers.AnalyticsManager;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Provides a menu for admins to view charts and data about the system's performance.
 * This class lets admins check how many bookings are made, see which flights are full, 
 * and understand who their typical passengers are.
 */
public class AdminAnalyticsUI {

    private final Scanner scanner;
    private final AnalyticsManager analyticsManager;

    /**
     * Sets up the analytics menu using a Scanner for reading user input.
     *
     * @param scanner reads text typed by the admin in the console
     */
    public AdminAnalyticsUI(Scanner scanner) {
        this.scanner = scanner;
        this.analyticsManager = AnalyticsManager.getInstance();
    }

    /**
     * Shows the main analytics menu and keeps it running in a loop.
     * Directs the admin to specific report categories based on their choice.
     */
    public void startAnalyticsFlow() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== PLATFORM ANALYTICS ===");
            System.out.println("1. Booking Reports");
            System.out.println("2. Flight Performance Reports");
            System.out.println("3. Passenger Analytics");
            System.out.println("0. Go Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleBookingReports();
                    break;
                case "2":
                    handleFlightPerformanceReports();
                    break;
                case "3":
                    handlePassengerAnalytics();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Shows detailed stats related to bookings, like total money earned or how many flights get cancelled.
     */
    private void handleBookingReports() {
        System.out.println("\n--- 13.1 Booking Reports ---");
        System.out.println("1. Daily Booking Report (Today)");
        System.out.println("2. Revenue Report by Date Range");
        System.out.println("3. Booking Trends by Route");
        System.out.println("4. Average Booking Value");
        System.out.println("5. Cancellation Rates");
        System.out.println("6. Payment Success Rates");
        System.out.println("0. Back");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.out.println("Bookings Today: " + analyticsManager.getDailyBookingCount());
                break;
            case "2":
                try {
                    System.out.print("Start Date (YYYY-MM-DD): ");
                    LocalDate start = LocalDate.parse(scanner.nextLine().trim());
                    System.out.print("End Date (YYYY-MM-DD): ");
                    LocalDate end = LocalDate.parse(scanner.nextLine().trim());
                    System.out.printf("Revenue: INR %.2f%n", analyticsManager.getRevenueByDateRange(start, end));
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format.");
                }
                break;
            case "3":
                System.out.println("Booking Trends by Route:");
                analyticsManager.getBookingTrendsByRoute().forEach((r, c) -> System.out.println(" - " + r + ": " + c + " bookings"));
                break;
            case "4":
                System.out.printf("Average Booking Value: INR %.2f%n", analyticsManager.calculateAverageBookingValue());
                break;
            case "5":
                System.out.printf("Cancellation Rate: %.2f%%%n", analyticsManager.getCancellationRate());
                break;
            case "6":
                System.out.printf("Payment Success Rate: %.2f%%%n", analyticsManager.getPaymentSuccessRate());
                break;
        }
    }

    /**
     * Shows stats about how well flights are doing, such as how full they are or when most people book.
     */
    private void handleFlightPerformanceReports() {
        System.out.println("\n--- 13.2 Flight Performance Reports ---");
        System.out.println("1. Flight Occupancy Rates");
        System.out.println("2. Revenue per Airline");
        System.out.println("3. Peak Booking Hours");
        System.out.println("0. Back");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.out.println("Flight Occupancy Rates:");
                analyticsManager.getFlightOccupancyRates().forEach((f, r) -> System.out.printf(" - %s: %.1f%%%n", f, r));
                break;
            case "2":
                System.out.println("Revenue by Airline:");
                analyticsManager.getRevenueByAirline().forEach((a, r) -> System.out.printf(" - %s: INR %.2f%n", a, r));
                break;
            case "3":
                System.out.println("Peak Booking Hours:");
                analyticsManager.getPeakBookingPeriods().forEach((h, c) -> System.out.println(" - Hour " + h + ":00 = " + c + " bookings"));
                break;
        }
    }

    /**
     * Shows info about the people flying, like where they are from or how often they fly with us.
     */
    private void handlePassengerAnalytics() {
        System.out.println("\n--- 13.3 Passenger Analytics ---");
        System.out.println("1. Passenger Demographics (Nationality)");
        System.out.println("2. Number of Repeat Customers");
        System.out.println("3. Customer Lifetime Value (by User ID)");
        System.out.println("0. Back");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                System.out.println("Passenger Demographics:");
                analyticsManager.getPassengerDemographics().forEach((n, c) -> System.out.println(" - " + n + ": " + c + " passengers"));
                break;
            case "2":
                System.out.println("Repeat Customers: " + analyticsManager.getRepeatCustomers());
                break;
            case "3":
                System.out.print("Enter User ID: ");
                try {
                    int id = Integer.parseInt(scanner.nextLine().trim());
                    System.out.printf("Lifetime Value for User %d: INR %.2f%n", id, analyticsManager.getCustomerLifetimeValue(id));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID.");
                }
                break;
        }
    }
}
