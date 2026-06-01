package com.airline.skybooker.managers;

import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.User;
import com.airline.skybooker.models.Passenger;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * Data aggregator for system-wide platform analytics and reporting.
 * Processes operational data from bookings, flights, and user activities.
 */
public class AnalyticsManager {

    private static volatile AnalyticsManager instance;

    private AnalyticsManager() {}

    /**
     * Retrieves the singleton instance of the AnalyticsManager.
     *
     * @return the singleton AnalyticsManager instance
     */
    public static AnalyticsManager getInstance() {
        if (instance == null) {
            synchronized (AnalyticsManager.class) {
                if (instance == null) {
                    instance = new AnalyticsManager();
                }
            }
        }
        return instance;
    }


    // 13.1 Booking Reports
    /**
     * Aggregates the total revenue generated from all confirmed bookings.
     *
     * @return the sum of total fares across all confirmed bookings
     */
    public double calculateTotalRevenue() {
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getStatus().equals("CONFIRMED"))
                .mapToDouble(Booking::getTotalFare)
                .sum();
    }

    /**
     * Computes the average revenue amount per confirmed booking.
     *
     * @return the average booking fare, or 0.0 if no confirmed bookings exist
     */
    public double calculateAverageBookingValue() {
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getStatus().equals("CONFIRMED"))
                .mapToDouble(Booking::getTotalFare)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculates the percentage of processed bookings that resulted in cancellations.
     *
     * @return the cancellation rate as a percentage (0.0 to 100.0)
     */
    public double getCancellationRate() {
        long totalProcessed = BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getStatus().equals("CONFIRMED") || b.getStatus().equals("REFUNDED"))
                .count();
        if (totalProcessed == 0) return 0.0;
        long totalRefunded = BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getStatus().equals("REFUNDED"))
                .count();
        return ((double) totalRefunded / totalProcessed) * 100.0;
    }

    /**
     * Counts the total number of bookings initiated on the current system date.
     *
     * @return the number of bookings registered today
     */
    public long getDailyBookingCount() {
        LocalDate today = LocalDate.now();
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getBookedAt().toLocalDate().equals(today))
                .count();
    }

    /**
     * Computes the total confirmed revenue accumulated within a specified date boundary.
     *
     * @param start the inclusive start date of the reporting period
     * @param end   the inclusive end date of the reporting period
     * @return the total revenue generated within the date range
     */
    public double getRevenueByDateRange(LocalDate start, LocalDate end) {
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getStatus().equals("CONFIRMED"))
                .filter(b -> !b.getBookedAt().toLocalDate().isBefore(start) && !b.getBookedAt().toLocalDate().isAfter(end))
                .mapToDouble(Booking::getTotalFare)
                .sum();
    }

    /**
     * Groups and counts bookings partitioned by origin and destination IATA route combinations.
     *
     * @return a map associating each route (e.g., "DEL-BOM") with its total booking count
     */
    public Map<String, Long> getBookingTrendsByRoute() {
        FlightManager fm = FlightManager.getInstance();
        return BookingManager.getInstance().getAllBookings().stream()
                .map(b -> fm.getAllFlights().stream().filter(f -> f.getFlightId() == b.getFlightId()).findFirst().orElse(null))
                .filter(f -> f != null)
                .collect(Collectors.groupingBy(
                        f -> f.getOrigin().getIataCode() + "-" + f.getDestination().getIataCode(),
                        Collectors.counting()
                ));
    }

    /**
     * Calculates the success ratio of all attempted payment transactions.
     *
     * @return the percentage of successful payments, or 100.0 if no transactions exist
     */
    public double getPaymentSuccessRate() {
        int success = PaymentManager.getInstance().getSuccessfulTransactions();
        int failed = PaymentManager.getInstance().getFailedTransactions();
        int total = success + failed;
        if (total == 0) return 100.0;
        return ((double) success / total) * 100.0;
    }


    //  Flight Performance Reports
    /**
     * Aggregates confirmed booking revenue partitioned by the operating airline.
     *
     * @return a map linking each airline name to its total generated revenue
     */
    public Map<String, Double> getRevenueByAirline() {
        FlightManager fm = FlightManager.getInstance();
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getStatus().equals("CONFIRMED"))
                .collect(Collectors.groupingBy(
                    b -> {
                        Flight f = fm.getAllFlights().stream()
                                .filter(flight -> flight.getFlightId() == b.getFlightId())
                                .findFirst().orElse(null);
                        return (f != null) ? f.getAirline().getName() : "Unknown Airline";
                    },
                    Collectors.summingDouble(Booking::getTotalFare)
                ));
    }

    /**
     * Computes the percentage of occupied seats against total capacity for all registered flights.
     *
     * @return a map linking flight numbers to their respective occupancy rates (0.0 to 100.0)
     */
    public Map<String, Double> getFlightOccupancyRates() {
        return FlightManager.getInstance().getAllFlights().stream()
                .collect(Collectors.toMap(
                    Flight::getFlightNumber,
                    f -> f.getTotalCapacity() == 0 ? 0.0 : ((double) (f.getTotalCapacity() - f.getAvailableSeats()) / f.getTotalCapacity()) * 100.0
                ));
    }

    /**
     * Identifies booking frequency partitioned by the hour of the day to detect peak periods.
     *
     * @return a map associating the hour of day (0-23) with its booking count
     */
    public Map<Integer, Long> getPeakBookingPeriods() {
        return BookingManager.getInstance().getAllBookings().stream()
                .collect(Collectors.groupingBy(
                    b -> b.getBookedAt().getHour(),
                    Collectors.counting()
                ));
    }


    // Passenger Analytics
    /**
     * Analyzes user nationality distribution among registered passenger profiles.
     *
     * @return a map linking nationalities to their respective passenger counts
     */
    public Map<String, Long> getPassengerDemographics() {
        return AuthenticationManager.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Passenger)
                .map(u -> (Passenger) u)
                .collect(Collectors.groupingBy(
                    p -> p.getNationality() != null ? p.getNationality() : "Unknown",
                    Collectors.counting()
                ));
    }

    /**
     * Calculates the number of unique passengers possessing more than one booking record.
     *
     * @return the count of repeat customers
     */
    public long getRepeatCustomers() {
        Map<Integer, Long> bookingsPerUser = BookingManager.getInstance().getAllBookings().stream()
                .collect(Collectors.groupingBy(Booking::getUserId, Collectors.counting()));
                
        return bookingsPerUser.values().stream()
                .filter(count -> count > 1)
                .count();
    }

    /**
     * Evaluates the cumulative confirmed revenue contributed by a specific user.
     *
     * @param userId the unique identifier of the target user
     * @return the total revenue generated by the user's confirmed bookings
     */
    public double getCustomerLifetimeValue(int userId) {
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getUserId() == userId && b.getStatus().equals("CONFIRMED"))
                .mapToDouble(Booking::getTotalFare)
                .sum();
    }
}
