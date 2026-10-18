package com.flashflow;

import com.flashflow.dto.OrderRequest;
import com.flashflow.dto.OrderResponse;
import com.flashflow.dto.PaymentWebhookRequest;
import com.flashflow.model.OrderStatus;
import com.flashflow.model.Product;
import com.flashflow.repository.OrderRepository;
import com.flashflow.repository.ProductRepository;
import com.flashflow.service.OrderService;
import com.flashflow.service.PaymentWebhookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class FailureRecoveryIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentWebhookService webhookService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Failure Recovery: Payment failure triggers compensating event and replenishes inventory")
    void testPaymentFailureInventoryRollback() {
        int initialStock = 5;
        int orderQuantity = 2;

        Product product = new Product(
                "Recovery Test Item",
                "Product for verifying failure recovery rollback",
                new BigDecimal("299.99"),
                initialStock,
                null,
                "Electronics"
        );
        Product saved = productRepository.save(product);
        Long productId = saved.getId();

        // 1. Place asynchronous order (reserves 2 units)
        OrderRequest orderReq = new OrderRequest(productId, orderQuantity, "buyer_failure@test.com");
        OrderResponse orderRes = orderService.createPendingOrder(orderReq, UUID.randomUUID().toString());
        String orderNumber = orderRes.getOrderNumber();

        // Check intermediate reserved stock
        Product productAfterReserve = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock - orderQuantity, productAfterReserve.getStockQuantity(), 
                "Stock should be reduced from 5 to 3 upon order creation");

        // 2. Simulate Payment Failure Webhook
        String webhookId = "wh_fail_" + UUID.randomUUID();
        String paymentId = "pay_fail_" + UUID.randomUUID();

        PaymentWebhookRequest webhookRequest = new PaymentWebhookRequest(
                webhookId,
                paymentId,
                orderNumber,
                new BigDecimal("599.98"),
                "FAILED",
                "mock_sig"
        );

        webhookService.processWebhook(webhookRequest);

        // 3. Verify Order Status transitioned to FAILED
        OrderStatus currentStatus = orderRepository.findByOrderNumber(orderNumber).orElseThrow().getStatus();
        assertEquals(OrderStatus.FAILED, currentStatus, "Order status should be FAILED");

        // 4. Verify Compensating Transaction / Inventory Restoration
        Product productAfterRollback = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock, productAfterRollback.getStockQuantity(), 
                "Stock must be fully replenished back to initial quantity (5) upon payment failure");
    }
}
