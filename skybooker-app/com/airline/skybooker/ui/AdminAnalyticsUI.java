package com.airline.skybooker.ui;

import com.airline.skybooker.managers.AnalyticsManager;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Command-line interface for platform analytics and reporting.
 * Provides administrators with access to booking trends, flight performance, and passenger metrics.
 */
public class AdminAnalyticsUI {

    private final Scanner scanner;
    private final AnalyticsManager analyticsManager;

    /**
     * Constructs the analytics interface with the provided input scanner.
     *
     * @param scanner the input reader for capturing administrator commands
     */
    public AdminAnalyticsUI(Scanner scanner) {
        this.scanner = scanner;
        this.analyticsManager = AnalyticsManager.getInstance();
    }

    /**
     * Initiates the main interactive loop for accessing analytics reports.
     * Presents categories of available reports and routes to the appropriate handler.
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
     * Presents booking-related reports including revenue, cancellation rates, and trends.
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
     * Presents flight-related performance metrics such as occupancy rates and airline revenue.
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
     * Presents passenger demographics and customer lifetime value metrics.
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
