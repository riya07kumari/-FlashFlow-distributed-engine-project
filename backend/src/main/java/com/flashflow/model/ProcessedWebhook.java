package com.flashflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_webhooks", indexes = {
    @Index(name = "idx_webhook_order", columnList = "orderNumber")
})
public class ProcessedWebhook {

    @Id
    @Column(nullable = false, unique = true)
    private String webhookId;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private String eventType;

    private BigDecimal amount;

    private String signature;

    @Column(nullable = false)
    private String status;

    @Column(length = 2000)
    private String payloadSummary;

    @Column(nullable = false, updatable = false)
    private LocalDateTime processedAt;

    public ProcessedWebhook() {
    }

    public ProcessedWebhook(String webhookId, String orderNumber, String eventType, 
                            BigDecimal amount, String signature, String status, String payloadSummary) {
        this.webhookId = webhookId;
        this.orderNumber = orderNumber;
        this.eventType = eventType;
        this.amount = amount;
        this.signature = signature;
        this.status = status;
        this.payloadSummary = payloadSummary;
        this.processedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPayloadSummary() {
        return payloadSummary;
    }

    public void setPayloadSummary(String payloadSummary) {
        this.payloadSummary = payloadSummary;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
