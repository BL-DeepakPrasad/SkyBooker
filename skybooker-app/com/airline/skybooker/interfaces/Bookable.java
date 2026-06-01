package com.airline.skybooker.interfaces;

/**
 * Contract for entities that undergo a reservation lifecycle.
 */
public interface Bookable {
    /**
     * Finalizes the reservation state and locks the associated inventory.
     */
    void confirm();
}
