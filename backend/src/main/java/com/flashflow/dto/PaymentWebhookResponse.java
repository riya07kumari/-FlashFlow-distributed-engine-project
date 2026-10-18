package com.flashflow.dto;

import java.time.LocalDateTime;

public class PaymentWebhookResponse {

    private String status; // "PROCESSED", "ALREADY_PROCESSED", "REJECTED"
    private String webhookId;
    private String orderNumber;
    private String message;
    private LocalDateTime timestamp;

    public PaymentWebhookResponse() {
    }

    public PaymentWebhookResponse(String status, String webhookId, String orderNumber, String message) {
        this.status = status;
        this.webhookId = webhookId;
        this.orderNumber = orderNumber;
        this.message = message;
        this.timestamp = LocalDateTime.now();
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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
}
