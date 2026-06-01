package com.airline.skybooker.payments;

import com.airline.skybooker.interfaces.Payable;

/**
 * Abstract base class for processing payments.
 * Ensures all payment methods conform to the Payable interface.
 */
public abstract class AbstractPayment implements Payable {
    // Shared payment functionality could go here in the future
}
