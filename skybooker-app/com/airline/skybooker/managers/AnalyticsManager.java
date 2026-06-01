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
 * Centralized platform analytics and reporting.
 */
public class AnalyticsManager {

    private static volatile AnalyticsManager instance;

    private AnalyticsManager() {}

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
    public double calculateTotalRevenue() {
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getStatus().equals("CONFIRMED"))
                .mapToDouble(Booking::getTotalFare)
                .sum();
    }

    public double calculateAverageBookingValue() {
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getStatus().equals("CONFIRMED"))
                .mapToDouble(Booking::getTotalFare)
                .average()
                .orElse(0.0);
    }

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

    public long getDailyBookingCount() {
        LocalDate today = LocalDate.now();
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getBookedAt().toLocalDate().equals(today))
                .count();
    }

    public double getRevenueByDateRange(LocalDate start, LocalDate end) {
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getStatus().equals("CONFIRMED"))
                .filter(b -> !b.getBookedAt().toLocalDate().isBefore(start) && !b.getBookedAt().toLocalDate().isAfter(end))
                .mapToDouble(Booking::getTotalFare)
                .sum();
    }

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

    public double getPaymentSuccessRate() {
        int success = PaymentManager.getInstance().getSuccessfulTransactions();
        int failed = PaymentManager.getInstance().getFailedTransactions();
        int total = success + failed;
        if (total == 0) return 100.0;
        return ((double) success / total) * 100.0;
    }


    //  Flight Performance Reports
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

    public Map<String, Double> getFlightOccupancyRates() {
        return FlightManager.getInstance().getAllFlights().stream()
                .collect(Collectors.toMap(
                    Flight::getFlightNumber,
                    f -> f.getTotalCapacity() == 0 ? 0.0 : ((double) (f.getTotalCapacity() - f.getAvailableSeats()) / f.getTotalCapacity()) * 100.0
                ));
    }

    public Map<Integer, Long> getPeakBookingPeriods() {
        return BookingManager.getInstance().getAllBookings().stream()
                .collect(Collectors.groupingBy(
                    b -> b.getBookedAt().getHour(),
                    Collectors.counting()
                ));
    }


    // Passenger Analytics
    public Map<String, Long> getPassengerDemographics() {
        return AuthenticationManager.getInstance().getAllUsers().stream()
                .filter(u -> u instanceof Passenger)
                .map(u -> (Passenger) u)
                .collect(Collectors.groupingBy(
                    p -> p.getNationality() != null ? p.getNationality() : "Unknown",
                    Collectors.counting()
                ));
    }

    public long getRepeatCustomers() {
        Map<Integer, Long> bookingsPerUser = BookingManager.getInstance().getAllBookings().stream()
                .collect(Collectors.groupingBy(Booking::getUserId, Collectors.counting()));
                
        return bookingsPerUser.values().stream()
                .filter(count -> count > 1)
                .count();
    }

    public double getCustomerLifetimeValue(int userId) {
        return BookingManager.getInstance().getAllBookings().stream()
                .filter(b -> b.getUserId() == userId && b.getStatus().equals("CONFIRMED"))
                .mapToDouble(Booking::getTotalFare)
                .sum();
    }
}
