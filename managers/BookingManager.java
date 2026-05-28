package com.airline.skybooker.managers;

import com.airline.skybooker.enums.SeatStatus;
import com.airline.skybooker.enums.TripType;
import com.airline.skybooker.exception.BookingNotFoundException;
import com.airline.skybooker.exception.SeatUnavailableException;
import com.airline.skybooker.interfaces.Bookable;
import com.airline.skybooker.models.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class BookingManager implements Bookable {
    private static volatile BookingManager instance;

    // priority queue for Express booking
    private PriorityBlockingQueue<Booking> processingQueue;
    private Map<String, Booking> confirmedBookings;
    private Map<String, Booking> allBookings;

    // Concurrency resource map
    private Map<String, Seat> seatDatabase;

    private int getPriorityWeight(TripType tripType) {
        switch(tripType) {
            case VIP: return 1;
            case EXPRESS: return 2;
            case REGULAR: return 3;
            case STANDBY: return 4;
            default: return 3;
        }
    }

    private double calculateFeeOnTripType(TripType tripType) {
        switch (tripType) {
            case VIP: return 150.0;
            case EXPRESS: return 50.0;
            case STANDBY: return -20.0;
            case REGULAR:
            default: return 0.0;
        }
    }

    private BookingManager() {
        this.confirmedBookings = new ConcurrentHashMap<>();
        this.allBookings = new ConcurrentHashMap<>();
        this.seatDatabase = new ConcurrentHashMap<>();
        this.seatDatabase.put("MOCK_12A", new Seat(123, null, "12A", "ECONOMY", 12, "A", true, false, false));

        Comparator<Booking> priorityComparator = (b1, b2) -> {
            int w1 = getPriorityWeight(b1.getTripType());
            int w2 = getPriorityWeight(b2.getTripType());
            if (w1 != w2) {
                return Integer.compare(w1, w2);
            }
            return b1.getBookedAt().compareTo(b2.getBookedAt());
        };

        this.processingQueue = new PriorityBlockingQueue<>(11, priorityComparator);
    }

    public static BookingManager getInstance() {
        if (instance == null) {
            synchronized (BookingManager.class) {
                if (instance == null) { instance = new BookingManager(); }
            }
        }
        return instance;
    }

    @Override
    public Seat allocateSeat(Flight flight, Passenger passenger, String preferredSeat) throws SeatUnavailableException {
        if (flight.getAvailableSeats() <= 0) {
            throw new SeatUnavailableException(preferredSeat, flight.getFlightNumber());
        }

        String seatKey = flight.getFlightNumber() + "_" + preferredSeat;
        seatDatabase.putIfAbsent(seatKey, new Seat((int)(Math.random() * 1000), flight, preferredSeat, "ECONOMY", 12, "B", false, false, false));

        Seat seat = seatDatabase.get(seatKey);
        if (seat == null) throw new SeatUnavailableException(preferredSeat, flight.getFlightNumber());

        synchronized (seat) {
            if (SeatStatus.AVAILABLE.equals(seat.getStatus())) {
                seat.setStatus(SeatStatus.LOCKED);
                passenger.setSeat(seat);
                flight.decrementSeats();
                return seat;
            } else {
                throw new SeatUnavailableException(preferredSeat, flight.getFlightNumber());
            }
        }
    }

    @Override
    public Booking createBooking(User user, Flight flight, List<Passenger> passengers, TripType tripType) {
        return createBooking(user, flight, null, passengers, tripType);
    }

    public Booking createBooking(User user, Flight outboundFlight, Flight returnFlight, List<Passenger> passengers, TripType tripType) {
        String bookingId = "BKG-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        double additionalFee = calculateFeeOnTripType(tripType);

        double baseCost = outboundFlight.getBasePrice();
        if (returnFlight != null) {
            baseCost += returnFlight.getBasePrice();
        }

        double totalCost = (baseCost * passengers.size()) + additionalFee;

        Booking booking = new Booking(bookingId, user.getUserId(), outboundFlight, returnFlight, passengers, tripType, totalCost);

        booking.proceedToNextState(); // Moves to PAYMENT_PENDING
        processingQueue.offer(booking);
        allBookings.put(bookingId, booking);
        return booking;
    }

    @Override
    public void confirmBooking(String bookingId) {
        Booking b = allBookings.get(bookingId);
        if (b != null) {
            b.proceedToNextState(); // Moves to CONFIRMED
            confirmedBookings.put(bookingId, b);
            System.out.println("Processed: " + b.getBookingId() + " (" + b.getTripType() + ")");
        } else {
            throw new BookingNotFoundException(bookingId);
        }
    }

    public void processQueue() {
        System.out.println("\n--- Processing Booking Queue ---");
        while (!processingQueue.isEmpty()) {
            Booking b = processingQueue.poll();
            confirmBooking(b.getBookingId());
        }
    }

    public List<Booking> getBookingsByUser(int userId) {
        return allBookings.values().stream()
                .filter(b -> b.getUserId() == userId)
                .collect(Collectors.toList());
    }
}