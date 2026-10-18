package com.flashflow.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class InventoryRollbackEvent implements Serializable {

    private String eventId;
    private String orderNumber;
    private Long productId;
    private Integer quantity;
    private String reason; // "PAYMENT_FAILED", "PAYMENT_TIMEOUT", "EXPIRED", "USER_CANCELLED"
    private LocalDateTime timestamp;

    public InventoryRollbackEvent() {
    }

    public InventoryRollbackEvent(String eventId, String orderNumber, Long productId, 
                                  Integer quantity, String reason) {
        this.eventId = eventId;
        this.orderNumber = orderNumber;
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "InventoryRollbackEvent{" +
                "eventId='" + eventId + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", reason='" + reason + '\'' +
                '}';
    }
}
