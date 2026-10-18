package com.flashflow.service;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * High-Speed In-Memory Inventory Pre-Check and Caching Service.
 * Shields PostgreSQL from extreme read/write bursts during flash sales by fast-failing
 * sold-out requests at the Redis layer.
 */
@Service
public class RedisInventoryService {

    private static final Logger log = LoggerFactory.getLogger(RedisInventoryService.class);
    private static final String STOCK_KEY_PREFIX = "flashflow:stock:product:";

    private final RedissonClient redissonClient;

    public RedisInventoryService(@Autowired(required = false) RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean isRedisAvailable() {
        return redissonClient != null && !redissonClient.isShutdown();
    }

    /**
     * Initializes or synchronizes the Redis stock counter with database stock.
     */
    public void syncStock(Long productId, int currentStock) {
        if (!isRedisAvailable()) {
            return;
        }
        try {
            RAtomicLong atomicStock = redissonClient.getAtomicLong(STOCK_KEY_PREFIX + productId);
            atomicStock.set(currentStock);
            log.info("[RedisInventory] Synchronized stock in Redis for product ID {}: {} units", productId, currentStock);
        } catch (Exception e) {
            log.warn("[RedisInventory] Failed to sync stock in Redis for product {}: {}", productId, e.getMessage());
        }
    }

    /**
     * Attempts atomic stock reservation directly in Redis.
     * Returns true if Redis stock was sufficient, or false if sold out.
     * If Redis is unavailable, returns true to allow database-level atomic control to evaluate.
     */
    public boolean tryPreDecrement(Long productId, int quantity) {
        if (!isRedisAvailable()) {
            return true; // Bypass to DB-level atomic control
        }

        try {
            RAtomicLong atomicStock = redissonClient.getAtomicLong(STOCK_KEY_PREFIX + productId);
            long remaining = atomicStock.addAndGet(-quantity);

            if (remaining < 0) {
                // Revert decrement
                atomicStock.addAndGet(quantity);
                log.debug("[RedisInventory] Fast-fail: Product {} out of stock in Redis cache", productId);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("[RedisInventory] Error during Redis pre-decrement for product {}: {}. Bypassing to DB.", productId, e.getMessage());
            return true;
        }
    }

    /**
     * Restores stock in Redis (e.g. upon payment failure, timeout, or cancellation).
     */
    public void replenishStock(Long productId, int quantity) {
        if (!isRedisAvailable()) {
            return;
        }
        try {
            RAtomicLong atomicStock = redissonClient.getAtomicLong(STOCK_KEY_PREFIX + productId);
            long updated = atomicStock.addAndGet(quantity);
            log.info("[RedisInventory] Replenished {} units in Redis for product {}. New cache stock: {}", 
                    quantity, productId, updated);
        } catch (Exception e) {
            log.warn("[RedisInventory] Failed to replenish stock in Redis for product {}: {}", productId, e.getMessage());
        }
    }

    public Long getCachedStock(Long productId) {
        if (!isRedisAvailable()) {
            return null;
        }
        try {
            RAtomicLong atomicStock = redissonClient.getAtomicLong(STOCK_KEY_PREFIX + productId);
            return atomicStock.isExists() ? atomicStock.get() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
