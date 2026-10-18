package com.flashflow;

import com.flashflow.dto.OrderRequest;
import com.flashflow.dto.OrderResponse;
import com.flashflow.dto.PaymentReconciliationResponse;
import com.flashflow.model.Order;
import com.flashflow.model.OrderStatus;
import com.flashflow.model.Product;
import com.flashflow.repository.OrderRepository;
import com.flashflow.repository.ProductRepository;
import com.flashflow.service.OrderService;
import com.flashflow.service.PaymentReconciliationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PaymentReconciliationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentReconciliationService reconciliationService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Payment Reconciliation: Expired pending orders are cancelled and stock is returned")
    void testReconciliationCancelsExpiredOrdersAndRestoresStock() {
        int initialStock = 10;
        int orderQuantity = 3;

        Product product = new Product(
                "Reconciliation Test Item",
                "Product for verifying automated payment reconciliation",
                new BigDecimal("199.99"),
                initialStock,
                null,
                "Accessories"
        );
        Product saved = productRepository.save(product);
        Long productId = saved.getId();

        // 1. Place asynchronous order (reserves 3 units, remaining stock = 7)
        OrderRequest orderReq = new OrderRequest(productId, orderQuantity, "reconciliation_user@test.com");
        OrderResponse orderRes = orderService.createPendingOrder(orderReq, UUID.randomUUID().toString());
        String orderNumber = orderRes.getOrderNumber();

        Product afterReserve = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock - orderQuantity, afterReserve.getStockQuantity(), 
                "Stock should be 7 after reserving 3 units");

        // 2. Artificially backdate order to simulate abandoned checkout (e.g. 10 minutes ago)
        Order order = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        order.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        orderRepository.save(order);

        // 3. Trigger reconciliation with a 0-minute threshold to sweep pending orders
        PaymentReconciliationResponse response = reconciliationService.runReconciliation(0);
        assertTrue(response.getExpiredCount() >= 1, "At least 1 expired order should be swept");
        assertTrue(response.getRestoredStockUnits() >= orderQuantity, "At least 3 stock units should be restored");

        // 4. Verify order state transitioned to EXPIRED
        Order refreshedOrder = orderRepository.findByOrderNumber(orderNumber).orElseThrow();
        assertEquals(OrderStatus.EXPIRED, refreshedOrder.getStatus(), "Order status must be EXPIRED");

        // 5. Verify database stock returned to 10
        Product productAfterRecon = productRepository.findById(productId).orElseThrow();
        assertEquals(initialStock, productAfterRecon.getStockQuantity(), 
                "Stock must be fully restored to 10 units after reconciliation");
    }
}
