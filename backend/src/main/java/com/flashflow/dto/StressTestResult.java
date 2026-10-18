package com.flashflow.dto;

public class StressTestResult {
    private Long productId;
    private String productTitle;
    private Integer initialStock;
    private Integer finalStock;
    private Integer totalRequests;
    private Integer successfulOrders;
    private Integer rejectedOrders;
    private Long durationMs;
    private Boolean zeroOversellingVerified;
    private String lockEngine;
    private String summary;

    public StressTestResult() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public Integer getInitialStock() { return initialStock; }
    public void setInitialStock(Integer initialStock) { this.initialStock = initialStock; }

    public Integer getFinalStock() { return finalStock; }
    public void setFinalStock(Integer finalStock) { this.finalStock = finalStock; }

    public Integer getTotalRequests() { return totalRequests; }
    public void setTotalRequests(Integer totalRequests) { this.totalRequests = totalRequests; }

    public Integer getSuccessfulOrders() { return successfulOrders; }
    public void setSuccessfulOrders(Integer successfulOrders) { this.successfulOrders = successfulOrders; }

    public Integer getRejectedOrders() { return rejectedOrders; }
    public void setRejectedOrders(Integer rejectedOrders) { this.rejectedOrders = rejectedOrders; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public Boolean getZeroOversellingVerified() { return zeroOversellingVerified; }
    public void setZeroOversellingVerified(Boolean zeroOversellingVerified) { this.zeroOversellingVerified = zeroOversellingVerified; }

    public String getLockEngine() { return lockEngine; }
    public void setLockEngine(String lockEngine) { this.lockEngine = lockEngine; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
