package com.flashflow.service;

import com.flashflow.dto.OrderRequest;
import com.flashflow.dto.StressTestRequest;
import com.flashflow.dto.StressTestResult;
import com.flashflow.exception.InsufficientStockException;
import com.flashflow.exception.ResourceNotFoundException;
import com.flashflow.lock.SmartLockCoordinator;
import com.flashflow.model.Product;
import com.flashflow.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StressTestService {

    private static final Logger log = LoggerFactory.getLogger(StressTestService.class);

    private final ProductRepository productRepository;
    private final OrderService orderService;
    private final RedisInventoryService redisInventoryService;
    private final SmartLockCoordinator lockCoordinator;

    public StressTestService(ProductRepository productRepository, 
                             OrderService orderService,
                             RedisInventoryService redisInventoryService,
                             SmartLockCoordinator lockCoordinator) {
        this.productRepository = productRepository;
        this.orderService = orderService;
        this.redisInventoryService = redisInventoryService;
        this.lockCoordinator = lockCoordinator;
    }

    /**
     * Executes a real-time multi-threaded concurrency simulation.
     * Fires N concurrent buyer threads against a single product at the exact same millisecond.
     */
    public StressTestResult runConcurrencyStressTest(StressTestRequest request) {
        Long productId = request.getProductId();
        int concurrentUsers = (request.getConcurrentUsers() != null && request.getConcurrentUsers() > 0) 
                ? request.getConcurrentUsers() : 50;

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        int initialStock = product.getStockQuantity();
        log.info("Starting Stress Test on Product: '{}' (Initial Stock: {}) with {} concurrent threads [Engine: {}]",
                product.getTitle(), initialStock, concurrentUsers, lockCoordinator.getActiveEngine());

        // Warm up / sync Redis cache with current database stock
        redisInventoryService.syncStock(productId, initialStock);

        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(concurrentUsers);

        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger rejectedOrders = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrentUsers; i++) {
            final int userIndex = i + 1;
            executor.submit(() -> {
                try {
                    // Wait until all threads are ready, then fire simultaneously
                    startGate.await();

                    OrderRequest orderReq = new OrderRequest(
                            productId,
                            1,
                            "stress_tester_" + userIndex + "@test.com"
                    );

                    orderService.createFlashSaleOrder(orderReq, UUID.randomUUID().toString());
                    successfulOrders.incrementAndGet();
                } catch (InsufficientStockException e) {
                    rejectedOrders.incrementAndGet();
                } catch (Exception e) {
                    rejectedOrders.incrementAndGet();
                    log.error("Unexpected error in stress test thread: {}", e.getMessage());
                } finally {
                    endGate.countDown();
                }
            });
        }

        // Release the gate: all threads fire simultaneously!
        startGate.countDown();

        try {
            boolean completed = endGate.await(15, TimeUnit.SECONDS);
            if (!completed) {
                log.warn("Stress test timed out after 15 seconds.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Stress test interrupted", e);
        } finally {
            executor.shutdown();
        }

        long duration = System.currentTimeMillis() - startTime;

        // Fetch refreshed final stock from database
        Product refreshed = productRepository.findById(productId).orElse(product);
        int finalStock = refreshed.getStockQuantity();

        boolean zeroOverselling = finalStock >= 0 && (initialStock - successfulOrders.get()) == finalStock;

        StressTestResult result = new StressTestResult();
        result.setProductId(productId);
        result.setProductTitle(product.getTitle());
        result.setInitialStock(initialStock);
        result.setFinalStock(finalStock);
        result.setTotalRequests(concurrentUsers);
        result.setSuccessfulOrders(successfulOrders.get());
        result.setRejectedOrders(rejectedOrders.get());
        result.setDurationMs(duration);
        result.setZeroOversellingVerified(zeroOverselling);
        result.setLockEngine(lockCoordinator.getActiveEngine());
        result.setSummary(String.format(
                "Processed %d parallel requests in %d ms using %s. %d orders succeeded, %d requests rejected. Zero overselling verified: %s",
                concurrentUsers, duration, lockCoordinator.getActiveEngine(), successfulOrders.get(), rejectedOrders.get(), zeroOverselling
        ));

        log.info("Stress test completed: {}", result.getSummary());
        return result;
    }
}
