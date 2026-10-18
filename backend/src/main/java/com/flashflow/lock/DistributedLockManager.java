package com.flashflow.lock;

/**
 * Universal Distributed Lock abstraction.
 * Allows transparent switching between Redisson (Redis) and thread-safe In-Memory locks.
 */
public interface DistributedLockManager {

    /**
     * Attempts to acquire a lock for the specified key within the wait timeout.
     *
     * @param lockKey unique resource identifier (e.g., "inventory:product:1")
     * @param waitTimeMs maximum time to wait for the lock
     * @param leaseTimeMs maximum time the lock is held before auto-releasing (lease safety against crashes)
     * @return true if acquired, false otherwise
     */
    boolean tryLock(String lockKey, long waitTimeMs, long leaseTimeMs);

    /**
     * Releases the lock if held by the current thread.
     *
     * @param lockKey unique resource identifier
     */
    void unlock(String lockKey);

    /**
     * Checks if the lock is held by the current thread.
     *
     * @param lockKey unique resource identifier
     * @return true if held by current thread
     */
    boolean isHeldByCurrentThread(String lockKey);
}
