package com.airline.skybooker.interfaces;

/**
 * An interface for things that can be booked.
 * It exists to make sure anything that can be reserved has a standard way to confirm it.
 */
public interface Bookable {
    /**
     * Finalizes the reservation state and locks the associated inventory.
     */
    void confirm();
}
