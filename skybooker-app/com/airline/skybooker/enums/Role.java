package com.airline.skybooker.enums;

/**
 * Represents the type of user logged into the system.
 * It exists to control what different people (like passengers vs. staff) are allowed to do.
 */
public enum Role {
    PASSENGER,
    ADMIN,
    AIRLINE_STAFF
}
