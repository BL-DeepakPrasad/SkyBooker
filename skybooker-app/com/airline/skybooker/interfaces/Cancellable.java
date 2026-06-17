package com.airline.skybooker.interfaces;

/**
 * An interface for things that can be cancelled.
 * It exists to provide a common way to undo an action or stop a process.
 */
public interface Cancellable {
    /**
     * Revokes the current entity state and releases any tied resources.
     * 
     * @return true if the termination executes successfully, false otherwise
     */
    boolean cancel();
}
