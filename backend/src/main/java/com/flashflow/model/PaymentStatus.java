package com.flashflow.model;

/**
 * Lifecycle states of a payment transaction.
 */
public enum PaymentStatus {
    INITIATED,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUNDED
}
