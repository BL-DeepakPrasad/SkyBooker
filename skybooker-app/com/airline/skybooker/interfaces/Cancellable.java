package com.airline.skybooker.interfaces;

/**
 * Interface defining operations for objects that can be cancelled.
 */
public interface Cancellable {
    /**
     * Cancels the entity.
     * @return true if cancellation was successful.
     */
    boolean cancel();
}
