package com.flashflow.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Intelligent Lock Coordinator.
 * Dispatches lock requests to Redis/Redisson when connected, with automatic, transparent fallback
 * to an In-Memory Fair Reentrant Lock Coordinator when running in standalone mode or during Redis outages.
 *
 * Tracks concurrency metrics (active locks, acquisition latency, contention rate).
 */
@Service
public class SmartLockCoordinator {

    private static final Logger log = LoggerFactory.getLogger(SmartLockCoordinator.class);

    private final RedissonDistributedLockManager redissonLockManager;
    private final InMemoryDistributedLockManager inMemoryLockManager;

    private final AtomicLong totalAcquisitionRequests = new AtomicLong(0);
    private final AtomicLong successfulAcquisitions = new AtomicLong(0);
    private final AtomicLong lockTimeouts = new AtomicLong(0);

    public SmartLockCoordinator(RedissonDistributedLockManager redissonLockManager, 
                                InMemoryDistributedLockManager inMemoryLockManager) {
        this.redissonLockManager = redissonLockManager;
        this.inMemoryLockManager = inMemoryLockManager;
    }

    /**
     * Tries to acquire a lock for a given resource.
     * Default lease time: 5000 ms (guarantees automatic release if worker crashes).
     *
     * @param resourceKey name or id of resource (e.g., "product:1")
     * @param waitTimeMs maximum time to wait for lock
     * @return true if locked successfully, false if timed out
     */
    public boolean tryLock(String resourceKey, long waitTimeMs) {
        return tryLock(resourceKey, waitTimeMs, 5000);
    }

    public boolean tryLock(String resourceKey, long waitTimeMs, long leaseTimeMs) {
        totalAcquisitionRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();
        boolean acquired = false;

        if (redissonLockManager.isAvailable()) {
            acquired = redissonLockManager.tryLock(resourceKey, waitTimeMs, leaseTimeMs);
        } else {
            acquired = inMemoryLockManager.tryLock(resourceKey, waitTimeMs, leaseTimeMs);
        }

        long duration = System.currentTimeMillis() - startTime;
        if (acquired) {
            successfulAcquisitions.incrementAndGet();
            log.debug("[LockCoordinator] Lock acquired for '{}' in {} ms (Engine: {})", 
                    resourceKey, duration, getActiveEngine());
        } else {
            lockTimeouts.incrementAndGet();
            log.warn("[LockCoordinator] Lock timeout for '{}' after {} ms (Engine: {})", 
                    resourceKey, duration, getActiveEngine());
        }

        return acquired;
    }

    public void unlock(String resourceKey) {
        if (redissonLockManager.isAvailable()) {
            redissonLockManager.unlock(resourceKey);
        }
        // Always attempt in-memory unlock as safety measure
        inMemoryLockManager.unlock(resourceKey);
    }

    public boolean isLockedByCurrentThread(String resourceKey) {
        if (redissonLockManager.isAvailable()) {
            return redissonLockManager.isHeldByCurrentThread(resourceKey);
        }
        return inMemoryLockManager.isHeldByCurrentThread(resourceKey);
    }

    public String getActiveEngine() {
        return redissonLockManager.isAvailable() ? "REDIS_REDISSON" : "IN_MEMORY_CONCURRENT";
    }

    public long getTotalAcquisitions() {
        return successfulAcquisitions.get();
    }

    public long getTotalTimeouts() {
        return lockTimeouts.get();
    }
}
