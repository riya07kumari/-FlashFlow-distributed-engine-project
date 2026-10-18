package com.flashflow.service;

import com.flashflow.dto.PaymentReconciliationResponse;
import com.flashflow.event.InventoryRollbackEvent;
import com.flashflow.model.Order;
import com.flashflow.model.OrderStatus;
import com.flashflow.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Reconciliation & Timeout Sweeper Engine.
 * Scans for abandoned or stuck 'PENDING_PAYMENT' orders, cancels them, and
 * issues asynchronous compensating rollback events via Apache Kafka to return reserved stock.
 */
@Service
public class PaymentReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationService.class);

    private final OrderRepository orderRepository;
    private final KafkaEventPublisher eventPublisher;

    @Value("${flashflow.reconciliation.timeout-minutes:2}")
    private int defaultTimeoutMinutes;

    public PaymentReconciliationService(OrderRepository orderRepository, KafkaEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Periodic scheduled reconciliation cron running every 30 seconds.
     */
    @Scheduled(fixedDelayString = "${flashflow.reconciliation.interval-ms:30000}")
    public void scheduledReconciliation() {
        try {
            runReconciliation(defaultTimeoutMinutes);
        } catch (Exception e) {
            log.error("[Reconciliation] Error during scheduled reconciliation run: {}", e.getMessage(), e);
        }
    }

    /**
     * Executes reconciliation with a configurable expiration timeout window.
     *
     * @param timeoutMinutes orders pending older than this threshold are expired
     * @return summary response containing scanned, expired, and restored counts
     */
    @Transactional
    public PaymentReconciliationResponse runReconciliation(int timeoutMinutes) {
        LocalDateTime cutoff = timeoutMinutes > 0 ? 
                LocalDateTime.now().minusMinutes(timeoutMinutes) : 
                LocalDateTime.now().plusSeconds(2);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING_PAYMENT, cutoff);

        int scannedCount = expiredOrders.size();
        int expiredCount = 0;
        int restoredStockUnits = 0;

        for (Order order : expiredOrders) {
            log.info("[Reconciliation] Order {} has exceeded payment timeout threshold (created: {}). Cancelling...", 
                    order.getOrderNumber(), order.getCreatedAt());

            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
            expiredCount++;
            restoredStockUnits += order.getQuantity();

            // Emit asynchronous inventory rollback to Kafka
            InventoryRollbackEvent rollbackEvent = new InventoryRollbackEvent(
                    null,
                    order.getOrderNumber(),
                    order.getProductId(),
                    order.getQuantity(),
                    "PAYMENT_TIMEOUT_EXPIRED"
            );
            eventPublisher.publishInventoryRollbackEvent(rollbackEvent);
        }

        if (expiredCount > 0) {
            log.info("[Reconciliation] Cycle finished: {} orders expired, {} stock units queued for replenishment.", 
                    expiredCount, restoredStockUnits);
        }

        return new PaymentReconciliationResponse(
                scannedCount,
                expiredCount,
                restoredStockUnits,
                "Reconciliation executed successfully for timeout: " + timeoutMinutes + " minutes."
        );
    }
}
