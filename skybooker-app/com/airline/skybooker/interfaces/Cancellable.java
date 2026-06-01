package com.airline.skybooker.interfaces;

/**
 * Contract for operations involving the termination of an active entity or workflow.
 */
public interface Cancellable {
    /**
     * Revokes the current entity state and releases any tied resources.
     * 
     * @return true if the termination executes successfully, false otherwise
     */
    boolean cancel();
}
