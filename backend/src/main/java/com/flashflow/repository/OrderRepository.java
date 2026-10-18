package com.flashflow.repository;

import com.flashflow.model.Order;
import com.flashflow.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findTop20ByOrderByCreatedAtDesc();

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoffTime);

    long countByProductId(Long productId);

    long countByProductIdAndStatus(Long productId, OrderStatus status);
}
