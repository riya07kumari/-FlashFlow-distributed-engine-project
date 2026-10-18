package com.flashflow;

import com.flashflow.dto.OrderRequest;
import com.flashflow.exception.InsufficientStockException;
import com.flashflow.model.OrderStatus;
import com.flashflow.model.Product;
import com.flashflow.repository.OrderRepository;
import com.flashflow.repository.ProductRepository;
import com.flashflow.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ConcurrentCheckoutStressTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long testProductId;
    private final int INITIAL_STOCK = 10;
    private final int CONCURRENT_BUYERS = 100;

    @BeforeEach
    void setUp() {
        Product product = new Product(
                "Stress-Test GPU RTX 5090",
                "High contention item for concurrency validation",
                new BigDecimal("999.99"),
                INITIAL_STOCK,
                null,
                "Hardware"
        );
        Product saved = productRepository.save(product);
        testProductId = saved.getId();
    }

    @Test
    @DisplayName("Validate 0 Overselling: 100 concurrent threads racing for 10 items")
    void testConcurrentCheckoutZeroOverselling() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_BUYERS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(CONCURRENT_BUYERS);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        for (int i = 0; i < CONCURRENT_BUYERS; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    // Align all threads to fire simultaneously
                    startGate.await();

                    OrderRequest request = new OrderRequest(testProductId, 1, "buyer_" + index + "@test.com");
                    orderService.createFlashSaleOrder(request, UUID.randomUUID().toString());
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    rejectedCount.incrementAndGet();
                } catch (Exception e) {
                    rejectedCount.incrementAndGet();
                } finally {
                    endGate.countDown();
                }
            });
        }

        // Fire all 100 threads at the exact same instant
        startGate.countDown();

        boolean finished = endGate.await(15, TimeUnit.SECONDS);
        assertTrue(finished, "Stress test completed within the timeout period");
        executor.shutdown();

        // 1. Assertions on success vs rejected counts
        assertEquals(INITIAL_STOCK, successCount.get(), 
                "Exactly " + INITIAL_STOCK + " orders must succeed because stock was " + INITIAL_STOCK);
        assertEquals(CONCURRENT_BUYERS - INITIAL_STOCK, rejectedCount.get(), 
                "Exactly " + (CONCURRENT_BUYERS - INITIAL_STOCK) + " orders must be rejected as out-of-stock");

        // 2. Assertions on Database State
        Product refreshed = productRepository.findById(testProductId).orElseThrow();
        assertEquals(0, refreshed.getStockQuantity(), 
                "Final inventory in database must be exactly 0 (No negative stock / zero overselling)");

        long confirmedOrdersInDb = orderRepository.countByProductIdAndStatus(testProductId, OrderStatus.CONFIRMED);
        assertEquals(INITIAL_STOCK, confirmedOrdersInDb, 
                "Database must contain exactly " + INITIAL_STOCK + " confirmed orders for this product");
    }
}
