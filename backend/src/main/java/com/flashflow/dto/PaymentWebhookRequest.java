package com.flashflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentWebhookRequest {

    @NotBlank(message = "Webhook ID is required for idempotency verification")
    private String webhookId;

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Payment status is required (SUCCESS or FAILED)")
    private String status;

    private String signature;

    public PaymentWebhookRequest() {
    }

    public PaymentWebhookRequest(String webhookId, String paymentId, String orderNumber, 
                                 BigDecimal amount, String status, String signature) {
        this.webhookId = webhookId;
        this.paymentId = paymentId;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.status = status;
        this.signature = signature;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
