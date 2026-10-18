package com.flashflow.repository;

import com.flashflow.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);

    /**
     * Atomic Stock Decrement at DB level.
     * Returns 1 if stock was decremented successfully, or 0 if insufficient stock.
     * Prevents race conditions and overselling directly within the database engine.
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :id AND p.stockQuantity >= :quantity")
    int decrementStockAtomic(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * Atomic Stock Increment (Compensating Transaction / Rollback).
     * Safely releases reserved stock back to the inventory pool upon payment failure or order expiration.
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :id")
    int incrementStockAtomic(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = :stock WHERE p.id = :id")
    int resetStock(@Param("id") Long id, @Param("stock") Integer stock);
}
