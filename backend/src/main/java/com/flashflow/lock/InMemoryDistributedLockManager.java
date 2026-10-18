package com.flashflow.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe Fair In-Memory Lock Coordinator.
 * Used for zero-dependency local testing and as a fail-safe fallback when Redis is unreachable.
 */
@Component("inMemoryLockManager")
public class InMemoryDistributedLockManager implements DistributedLockManager {

    private static final Logger log = LoggerFactory.getLogger(InMemoryDistributedLockManager.class);
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String lockKey, long waitTimeMs, long leaseTimeMs) {
        ReentrantLock lock = locks.computeIfAbsent(lockKey, k -> new ReentrantLock(true));
        try {
            boolean acquired = lock.tryLock(waitTimeMs, TimeUnit.MILLISECONDS);
            if (acquired) {
                log.debug("[InMemoryLock] Acquired lock for key: {}", lockKey);
            } else {
                log.warn("[InMemoryLock] Contention timeout for key: {} (waited {} ms)", lockKey, waitTimeMs);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[InMemoryLock] Lock acquisition interrupted for key: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) {
        ReentrantLock lock = locks.get(lockKey);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("[InMemoryLock] Released lock for key: {}", lockKey);
        }
    }

    @Override
    public boolean isHeldByCurrentThread(String lockKey) {
        ReentrantLock lock = locks.get(lockKey);
        return lock != null && lock.isHeldByCurrentThread();
    }
}
