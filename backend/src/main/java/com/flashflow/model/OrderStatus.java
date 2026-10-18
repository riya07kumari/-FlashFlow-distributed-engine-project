package com.flashflow.model;

/**
 * Order status representing the lifecycle of a Flash-Sale purchase.
 */
public enum OrderStatus {
    PENDING,
    PENDING_PAYMENT,
    CONFIRMED,
    FAILED,
    CANCELLED,
    EXPIRED,
    REFUNDED,
    OUT_OF_STOCK
}
