package com.flashflow.dto;

import java.time.LocalDateTime;

public class PaymentReconciliationResponse {

    private int scannedCount;
    private int expiredCount;
    private int restoredStockUnits;
    private String message;
    private LocalDateTime executionTime;

    public PaymentReconciliationResponse() {
    }

    public PaymentReconciliationResponse(int scannedCount, int expiredCount, int restoredStockUnits, String message) {
        this.scannedCount = scannedCount;
        this.expiredCount = expiredCount;
        this.restoredStockUnits = restoredStockUnits;
        this.message = message;
        this.executionTime = LocalDateTime.now();
    }

    public int getScannedCount() {
        return scannedCount;
    }

    public void setScannedCount(int scannedCount) {
        this.scannedCount = scannedCount;
    }

    public int getExpiredCount() {
        return expiredCount;
    }

    public void setExpiredCount(int expiredCount) {
        this.expiredCount = expiredCount;
    }

    public int getRestoredStockUnits() {
        return restoredStockUnits;
    }

    public void setRestoredStockUnits(int restoredStockUnits) {
        this.restoredStockUnits = restoredStockUnits;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }
}
