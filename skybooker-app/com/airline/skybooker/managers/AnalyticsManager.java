package com.airline.skybooker.managers;

import com.airline.skybooker.models.Flight;
import com.airline.skybooker.models.Booking;
import com.airline.skybooker.models.Passenger;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Generates business reports and statistics for the airline's management dashboard.
 * Calculates metrics like total revenue, cancellation rates, and peak booking times to help executives make data-driven decisions.
 */
public class AnalyticsManager {

    private static volatile AnalyticsManager instance;

    private AnalyticsManager() {}

    /**
     * Provides access to the single, shared AnalyticsManager instance.
     * Guarantees that reporting components use the same data aggregator.
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


    
    /**
     * Calculates the total money earned from all successful bookings.
     * Gives the business an overview of overall financial performance.
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
     * Calculates the average amount spent per confirmed booking.
     * Helps marketing teams understand typical customer spending habits.
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
     * Calculates what percentage of processed bookings were eventually canceled and refunded.
     * A high cancellation rate might indicate customer dissatisfaction or flexible booking policies being exploited.
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
     * Counts how many bookings were made today.
     * Used for daily operational monitoring and detecting sudden drops in system usage.
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
     * Calculates the total revenue earned during a specific time frame, like a holiday weekend or marketing campaign.
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
     * Counts how many bookings were made for each specific origin-to-destination flight route.
     * Helps the airline decide which routes need more planes and which routes should be discontinued.
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
     * Calculates the percentage of payment attempts that were successful.
     * A sudden drop might indicate an issue with the third-party payment gateway.
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
     * Calculates the total revenue earned by each individual airline operating on the platform.
     * Useful for distributing payouts in a multi-airline booking system.
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
     * Calculates the percentage of seats sold for every flight.
     * Helps determine if flights are flying mostly empty or are fully booked.
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
     * Groups booking counts by the hour of the day they were made.
     * Helps IT teams know when to schedule server maintenance (during quiet hours) and when to scale up servers (during peak hours).
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
     * Groups the platform's passengers by their nationality.
     * Allows the airline to tailor marketing campaigns to specific countries.
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
     * Counts how many unique users have booked more than one flight.
     * Acts as an indicator of customer loyalty and satisfaction.
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
     * Calculates the total amount of money a specific customer has spent on completed or upcoming flights.
     * Used to identify high-value VIP customers for special perks and loyalty rewards.
     *
     * @param userId the unique identifier of the target user
     * @return the total revenue generated by the user's confirmed bookings
     */
    public double getCustomerLifetimeValue(int userId) {
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getUserId() == userId)
                .filter(b -> {
                    String s = b.getStatus();
                    return s.equals("CONFIRMED") || s.equals("CHECKED_IN") || s.equals("BOARDING") || s.equals("COMPLETED");
                })
                .mapToDouble(Booking::getTotalFare)
                .sum();
    }
}
