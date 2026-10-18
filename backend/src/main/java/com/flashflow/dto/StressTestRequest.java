package com.flashflow.dto;

public class StressTestRequest {
    private Long productId;
    private Integer concurrentUsers = 50;

    public StressTestRequest() {}

    public StressTestRequest(Long productId, Integer concurrentUsers) {
        this.productId = productId;
        this.concurrentUsers = concurrentUsers;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getConcurrentUsers() { return concurrentUsers; }
    public void setConcurrentUsers(Integer concurrentUsers) { this.concurrentUsers = concurrentUsers; }
}
