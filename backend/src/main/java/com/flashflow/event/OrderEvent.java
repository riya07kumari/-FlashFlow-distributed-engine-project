package com.flashflow.event;

import com.flashflow.model.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderEvent implements Serializable {

    private String eventId;
    private String orderNumber;
    private Long productId;
    private String productTitle;
    private String userEmail;
    private Integer quantity;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String idempotencyKey;
    private LocalDateTime timestamp;

    public OrderEvent() {
    }

    public OrderEvent(String eventId, String orderNumber, Long productId, String productTitle, 
                      String userEmail, Integer quantity, BigDecimal totalAmount, 
                      OrderStatus status, String idempotencyKey) {
        this.eventId = eventId;
        this.orderNumber = orderNumber;
        this.productId = productId;
        this.productTitle = productTitle;
        this.userEmail = userEmail;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
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

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "eventId='" + eventId + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", status=" + status +
                '}';
    }
}
