package com.airline.skybooker.payments;

import com.airline.skybooker.interfaces.Payable;

/**
 * Core abstraction for financial transaction processors.
 * Establishes a unified integration point for diverse payment gateway implementations.
 */
public abstract class AbstractPayment implements Payable {
    // Shared payment functionality could go here in the future
}
