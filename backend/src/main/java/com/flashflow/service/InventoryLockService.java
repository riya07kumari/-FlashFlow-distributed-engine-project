package com.flashflow.service;

import com.flashflow.lock.SmartLockCoordinator;
import org.springframework.stereotype.Service;

/**
 * High-Performance Thread-Safe Lock Coordinator for Flash-Sale Concurrency.
 * Backed by SmartLockCoordinator (Redisson distributed lock with automatic In-Memory fallback).
 */
@Service
public class InventoryLockService {

    private final SmartLockCoordinator lockCoordinator;

    public InventoryLockService(SmartLockCoordinator lockCoordinator) {
        this.lockCoordinator = lockCoordinator;
    }

    public boolean tryLock(Long productId, long timeoutMillis) {
        return lockCoordinator.tryLock("product:" + productId, timeoutMillis);
    }

    public void unlock(Long productId) {
        lockCoordinator.unlock("product:" + productId);
    }

    public String getLockEngine() {
        return lockCoordinator.getActiveEngine();
    }
}
