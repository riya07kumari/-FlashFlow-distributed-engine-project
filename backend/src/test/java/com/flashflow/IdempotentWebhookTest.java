package com.flashflow;

import com.flashflow.dto.OrderRequest;
import com.flashflow.dto.OrderResponse;
import com.flashflow.dto.PaymentWebhookRequest;
import com.flashflow.dto.PaymentWebhookResponse;
import com.flashflow.model.OrderStatus;
import com.flashflow.model.PaymentRecord;
import com.flashflow.model.ProcessedWebhook;
import com.flashflow.model.Product;
import com.flashflow.repository.OrderRepository;
import com.flashflow.repository.PaymentRecordRepository;
import com.flashflow.repository.ProcessedWebhookRepository;
import com.flashflow.repository.ProductRepository;
import com.flashflow.service.OrderService;
import com.flashflow.service.PaymentWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class IdempotentWebhookTest {

    @Autowired
    private PaymentWebhookService webhookService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProcessedWebhookRepository webhookRepository;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    private String testOrderNumber;

    @BeforeEach
    void setUp() {
        Product product = new Product(
                "Idempotency Test Item",
                "Product for verifying webhook idempotency",
                new BigDecimal("150.00"),
                10,
                null,
                "Testing"
        );
        Product saved = productRepository.save(product);

        OrderRequest orderReq = new OrderRequest(saved.getId(), 1, "idempotent_user@test.com");
        OrderResponse orderRes = orderService.createPendingOrder(orderReq, UUID.randomUUID().toString());
        testOrderNumber = orderRes.getOrderNumber();
    }

    @Test
    @DisplayName("Sequential Idempotency: Duplicate webhook calls return ALREADY_PROCESSED")
    void testSequentialWebhookIdempotency() {
        String webhookId = "wh_seq_" + UUID.randomUUID();
        String paymentId = "pay_seq_" + UUID.randomUUID();

        PaymentWebhookRequest request = new PaymentWebhookRequest(
                webhookId,
                paymentId,
                testOrderNumber,
                new BigDecimal("150.00"),
                "SUCCESS",
                "mock_signature"
        );

        // First delivery: should be PROCESSED
        PaymentWebhookResponse firstResponse = webhookService.processWebhook(request);
        assertEquals("PROCESSED", firstResponse.getStatus());

        // Second delivery (duplicate retry): should be ALREADY_PROCESSED
        PaymentWebhookResponse duplicateResponse = webhookService.processWebhook(request);
        assertEquals("ALREADY_PROCESSED", duplicateResponse.getStatus());

        // Third delivery (duplicate retry): should still be ALREADY_PROCESSED
        PaymentWebhookResponse thirdResponse = webhookService.processWebhook(request);
        assertEquals("ALREADY_PROCESSED", thirdResponse.getStatus());

        // Verify only 1 webhook record and 1 payment record exists
        List<ProcessedWebhook> webhooks = webhookRepository.findByOrderNumber(testOrderNumber);
        assertEquals(1, webhooks.size(), "Only one webhook record should be persisted");

        List<PaymentRecord> payments = paymentRecordRepository.findByOrderNumber(testOrderNumber);
        assertEquals(1, payments.size(), "Only one payment transaction record should be persisted");

        // Verify order is confirmed
        assertEquals(OrderStatus.CONFIRMED, orderRepository.findByOrderNumber(testOrderNumber).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Concurrent Idempotency: Multiple threads firing identical webhookId simultaneously")
    void testConcurrentWebhookIdempotency() throws InterruptedException {
        String webhookId = "wh_concurrent_" + UUID.randomUUID();
        String paymentId = "pay_concurrent_" + UUID.randomUUID();

        PaymentWebhookRequest request = new PaymentWebhookRequest(
                webhookId,
                paymentId,
                testOrderNumber,
                new BigDecimal("150.00"),
                "SUCCESS",
                "mock_signature"
        );

        int concurrentCalls = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentCalls);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(concurrentCalls);

        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger alreadyProcessedCount = new AtomicInteger(0);

        for (int i = 0; i < concurrentCalls; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    PaymentWebhookResponse res = webhookService.processWebhook(request);
                    if ("PROCESSED".equals(res.getStatus())) {
                        processedCount.incrementAndGet();
                    } else if ("ALREADY_PROCESSED".equals(res.getStatus())) {
                        alreadyProcessedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Ignore interrupted
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        endGate.await();
        executor.shutdown();

        // Exactly one call must be PROCESSED, all other concurrent calls must be ALREADY_PROCESSED
        assertEquals(1, processedCount.get(), "Exactly one concurrent webhook call should be PROCESSED");
        assertEquals(concurrentCalls - 1, alreadyProcessedCount.get(), 
                "All redundant concurrent webhook calls should be ALREADY_PROCESSED");

        List<ProcessedWebhook> webhooks = webhookRepository.findByOrderNumber(testOrderNumber);
        assertEquals(1, webhooks.size());
    }
}
