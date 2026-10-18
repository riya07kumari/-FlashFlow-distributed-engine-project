package com.flashflow.service;

import com.flashflow.dto.OrderRequest;
import com.flashflow.dto.OrderResponse;
import com.flashflow.event.OrderEvent;
import com.flashflow.exception.InsufficientStockException;
import com.flashflow.exception.ResourceNotFoundException;
import com.flashflow.lock.SmartLockCoordinator;
import com.flashflow.model.Order;
import com.flashflow.model.OrderStatus;
import com.flashflow.model.Product;
import com.flashflow.repository.OrderRepository;
import com.flashflow.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final SmartLockCoordinator lockCoordinator;
    private final RedisInventoryService redisInventoryService;
    private final KafkaEventPublisher eventPublisher;

    public OrderService(ProductRepository productRepository, 
                        OrderRepository orderRepository, 
                        SmartLockCoordinator lockCoordinator,
                        RedisInventoryService redisInventoryService,
                        KafkaEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.lockCoordinator = lockCoordinator;
        this.redisInventoryService = redisInventoryService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes a Flash-Sale Order placement with multi-layered concurrency protection:
     * 1. Checks Idempotency Key to prevent duplicate submissions.
     * 2. Redis Fast-Fail Pre-Check to shield database from sold-out contention.
     * 3. Acquires granular distributed lock (Redisson / Fair Reentrant Lock).
     * 4. Executes PostgreSQL atomic stock decrement (stock >= quantity).
     * 5. Persists the confirmed order in PostgreSQL.
     * 6. Publishes asynchronous OrderEvent to Apache Kafka.
     */
    @Transactional
    public OrderResponse createFlashSaleOrder(OrderRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            idempotencyKey = UUID.randomUUID().toString();
        }

        // 1. Idempotency Check
        Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOrder.isPresent()) {
            Order o = existingOrder.get();
            log.info("[OrderService] Idempotent request detected for key: {}. Returning existing order.", idempotencyKey);
            return mapToResponse(o, "Order retrieved (Idempotent response)");
        }

        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();

        // 2. Redis Fast-Fail Pre-Check
        boolean stockAvailableInCache = redisInventoryService.tryPreDecrement(productId, quantity);
        if (!stockAvailableInCache) {
            log.warn("[OrderService] Flash-sale purchase fast-failed via Redis cache for product ID {}", productId);
            throw new InsufficientStockException("Sold out! Flash-sale stock exhausted for this item.");
        }

        // 3. Acquire Distributed Lock (Timeout: 2000ms, Lease: 5000ms)
        String lockKey = "product:" + productId;
        boolean lockAcquired = lockCoordinator.tryLock(lockKey, 2000, 5000);
        if (!lockAcquired) {
            // Revert Redis pre-decrement on lock timeout
            redisInventoryService.replenishStock(productId, quantity);
            throw new InsufficientStockException("System is experiencing heavy traffic contention. Please retry your purchase.");
        }

        try {
            // Fetch product to calculate total price and ensure existence
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

            // 4. Atomic Stock Decrement at PostgreSQL database level
            int updatedRows = productRepository.decrementStockAtomic(productId, quantity);
            if (updatedRows == 0) {
                // Stock exhausted in database: synchronize Redis cache and reject
                redisInventoryService.syncStock(productId, product.getStockQuantity());
                log.warn("[OrderService] Flash sale purchase rejected: Product ID {} is OUT OF STOCK in database", productId);
                throw new InsufficientStockException("Sold out! Insufficient stock for product: " + product.getTitle());
            }

            // 5. Create Confirmed Order
            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);

            Order order = new Order(
                    orderNumber,
                    productId,
                    product.getTitle(),
                    request.getUserEmail(),
                    quantity,
                    totalAmount,
                    OrderStatus.CONFIRMED,
                    idempotencyKey
            );

            Order savedOrder = orderRepository.save(order);
            log.info("[OrderService] Flash sale order placed: {} for product: {} (Lock Engine: {})", 
                    orderNumber, product.getTitle(), lockCoordinator.getActiveEngine());

            // 6. Asynchronous Event Stream to Apache Kafka
            OrderEvent event = new OrderEvent(
                    UUID.randomUUID().toString(),
                    savedOrder.getOrderNumber(),
                    savedOrder.getProductId(),
                    savedOrder.getProductTitle(),
                    savedOrder.getUserEmail(),
                    savedOrder.getQuantity(),
                    savedOrder.getTotalAmount(),
                    savedOrder.getStatus(),
                    savedOrder.getIdempotencyKey()
            );
            eventPublisher.publishOrderEvent(event);

            return mapToResponse(savedOrder, "Flash sale order placed successfully!");
        } finally {
            // Always release lock
            lockCoordinator.unlock(lockKey);
        }
    }

    /**
     * Creates an order in PENDING_PAYMENT state (used for asynchronous payment gateway flows).
     */
    @Transactional
    public OrderResponse createPendingOrder(OrderRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            idempotencyKey = UUID.randomUUID().toString();
        }

        Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
        if (existingOrder.isPresent()) {
            return mapToResponse(existingOrder.get(), "Order retrieved (Idempotent response)");
        }

        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();

        String lockKey = "product:" + productId;
        boolean lockAcquired = lockCoordinator.tryLock(lockKey, 2000, 5000);
        if (!lockAcquired) {
            throw new InsufficientStockException("High checkout traffic. Please retry.");
        }

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

            int updatedRows = productRepository.decrementStockAtomic(productId, quantity);
            if (updatedRows == 0) {
                throw new InsufficientStockException("Sold out! Insufficient stock for: " + product.getTitle());
            }

            BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            String orderNumber = "ORD-ASYNC-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);

            Order order = new Order(
                    orderNumber,
                    productId,
                    product.getTitle(),
                    request.getUserEmail(),
                    quantity,
                    totalAmount,
                    OrderStatus.PENDING_PAYMENT,
                    idempotencyKey
            );

            Order savedOrder = orderRepository.save(order);

            OrderEvent event = new OrderEvent(
                    UUID.randomUUID().toString(),
                    savedOrder.getOrderNumber(),
                    savedOrder.getProductId(),
                    savedOrder.getProductTitle(),
                    savedOrder.getUserEmail(),
                    savedOrder.getQuantity(),
                    savedOrder.getTotalAmount(),
                    savedOrder.getStatus(),
                    savedOrder.getIdempotencyKey()
            );
            eventPublisher.publishOrderEvent(event);

            return mapToResponse(savedOrder, "Pending payment order created. Stock reserved.");
        } finally {
            lockCoordinator.unlock(lockKey);
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRecentOrders() {
        return orderRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(o -> mapToResponse(o, "Success"))
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order, String message) {
        return new OrderResponse(
                order.getOrderNumber(),
                order.getProductId(),
                order.getProductTitle(),
                order.getUserEmail(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                message
        );
    }
}
