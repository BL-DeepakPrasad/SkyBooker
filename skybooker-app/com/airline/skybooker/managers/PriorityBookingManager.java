package com.airline.skybooker.managers;

import com.airline.skybooker.models.Booking;
import com.airline.skybooker.enums.BookingPriority;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Processes incoming bookings in a prioritized line (queue).
 * Customers who pay for "Express" booking skip to the front of the line, while "Regular" bookings wait their turn.
 * Automatically upgrades Regular bookings if they wait too long so they don't get stuck forever.
 */
public class PriorityBookingManager {

    private static volatile PriorityBookingManager instance;
    private final PriorityBlockingQueue<Booking> bookingQueue;
    
    // 10.2 Monitoring Metrics
    private int totalProcessed = 0;
    private int expressProcessed = 0;
    private int regularProcessed = 0;
    private long totalProcessingTimeMs = 0;
    
    // Starvation Prevention threshold: Max Wait Time (Simulated: 5000 ms)
    private static final long MAX_WAIT_TIME_MS = 5000;

    private PriorityBookingManager() {
        this.bookingQueue = new PriorityBlockingQueue<>();
    }

    /**
     * Provides access to the single, shared PriorityBookingManager instance.
     * Ensures all bookings wait in the exact same line across the entire application.
     *
     * @return the singleton PriorityBookingManager instance
     */
    public static PriorityBookingManager getInstance() {
        if (instance == null) {
            synchronized (PriorityBookingManager.class) {
                if (instance == null) {
                    instance = new PriorityBookingManager();
                }
            }
        }
        return instance;
    }

    /**
     * Adds a new booking to the back of the processing line.
     * Before adding it, checks if any older bookings have waited too long and need a priority upgrade.
     *
     * @param booking the Booking object to be added to the processing queue
     */
    public void enqueueBooking(Booking booking) {
        applyAgingAlgorithm(); // 10.2 Prevent starvation before adding new items
        bookingQueue.offer(booking);
        System.out.println("[QUEUE] Added " + booking.getPriority() + " booking to processing queue. Queue Size: " + bookingQueue.size());
    }

    /**
     * Checks if any Regular bookings have been waiting longer than the maximum allowed time.
     * If so, upgrades them to Express so they jump to the front of the line (prevents "starvation").
     */
    private void applyAgingAlgorithm() {
        long currentTime = System.currentTimeMillis();
        boolean reorderNeeded = false;

        for (Booking b : bookingQueue) {
            if (b.getPriority() == BookingPriority.REGULAR) {
                long waitTime = currentTime - b.getTimestamp();
                if (waitTime > MAX_WAIT_TIME_MS) {
                    b.setPriority(BookingPriority.EXPRESS);
                    System.out.println("[AGING] Upgraded Booking " + b.getPnrCode() + " to EXPRESS due to wait time.");
                    reorderNeeded = true;
                }
            }
        }

        // If priorities changed, we must rebuild the queue to trigger sorting
        if (reorderNeeded) {
            PriorityBlockingQueue<Booking> temp = new PriorityBlockingQueue<>(bookingQueue);
            bookingQueue.clear();
            bookingQueue.addAll(temp);
        }
    }

    /**
     * Goes through the line one by one, finalizing bookings based on who has the highest priority.
     * Simulates the time it takes the airline's servers to talk to the global ticketing systems.
     */
    public void processQueue() {
        System.out.println("\n--- PROCESSING BOOKING QUEUE (" + bookingQueue.size() + " pending) ---");
        
        while (!bookingQueue.isEmpty()) {
            Booking b = bookingQueue.poll();
            System.out.println("Processing -> " + b.getPriority() + " Booking | PNR: " + (b.getPnrCode() != null ? b.getPnrCode() : "N/A") + " | Timestamp: " + b.getTimestamp());
            
            long startTime = System.currentTimeMillis();
            
            // Simulating dedicated resources for Express bookings
            try {
                if (b.getPriority() == BookingPriority.EXPRESS) {
                    Thread.sleep(500); // Express processed quickly
                } else {
                    Thread.sleep(1000); // Regular takes longer
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long processTime = System.currentTimeMillis() - startTime;
            updateMetrics(b.getPriority(), processTime);
            
            System.out.println("   [SUCCESS] Finalized processing for " + b.getPriority() + " booking in " + processTime + "ms.");
        }
        System.out.println("--- QUEUE PROCESSING COMPLETE ---\n");
    }

    private void updateMetrics(BookingPriority priority, long processTime) {
        totalProcessed++;
        totalProcessingTimeMs += processTime;
        if (priority == BookingPriority.EXPRESS) {
            expressProcessed++;
        } else {
            regularProcessed++;
        }
    }

    /**
     * Prints out a summary of how quickly bookings are being processed.
     * Helps IT staff monitor system health and decide if they need to add more processing servers.
     */
    public void generateProcessingReport() {
        System.out.println("\n============================================");
        System.out.println("      PRIORITY PROCESSING REPORT            ");
        System.out.println("============================================");
        System.out.println("Current Queue Size:       " + bookingQueue.size());
        System.out.println("Total Bookings Processed: " + totalProcessed);
        System.out.println("  - EXPRESS Processed:    " + expressProcessed);
        System.out.println("  - REGULAR Processed:    " + regularProcessed);
        
        long avgTime = totalProcessed > 0 ? totalProcessingTimeMs / totalProcessed : 0;
        System.out.println("Avg Processing Time:      " + avgTime + "ms");
        System.out.println("============================================\n");
    }
}
