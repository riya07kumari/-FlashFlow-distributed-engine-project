package com.flashflow.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentEvent implements Serializable {

    private String eventId;
    private String paymentId;
    private String orderNumber;
    private BigDecimal amount;
    private String status; // "SUCCESS", "FAILED"
    private String webhookId;
    private String message;
    private LocalDateTime timestamp;

    public PaymentEvent() {
    }

    public PaymentEvent(String eventId, String paymentId, String orderNumber, BigDecimal amount, 
                        String status, String webhookId, String message) {
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.status = status;
        this.webhookId = webhookId;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "eventId='" + eventId + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                '}';
    }
}
