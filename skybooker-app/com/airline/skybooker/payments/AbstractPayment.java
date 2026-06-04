package com.airline.skybooker.payments;

import com.airline.skybooker.interfaces.Payable;

/**
 * Base class for all payment types.
 * It exists so the rest of the application can handle payments without needing to know the specific payment method (like credit card or UPI).
 */
public abstract class AbstractPayment implements Payable {
    // Shared payment functionality could go here in the future
}
