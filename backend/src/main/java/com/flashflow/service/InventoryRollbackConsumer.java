package com.flashflow.service;

import com.flashflow.config.KafkaTopicConfig;
import com.flashflow.event.InventoryRollbackEvent;
import com.flashflow.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compensating Transaction Consumer.
 * Listens to Kafka Inventory Rollback events and safely replenishes stock in PostgreSQL and Redis.
 */
@Service
public class InventoryRollbackConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryRollbackConsumer.class);

    private final ProductRepository productRepository;
    private final RedisInventoryService redisInventoryService;

    // In-memory deduplication set for rollback events (or backed by DB)
    private final Set<String> processedRollbacks = ConcurrentHashMap.newKeySet();

    public InventoryRollbackConsumer(ProductRepository productRepository, 
                                     RedisInventoryService redisInventoryService) {
        this.productRepository = productRepository;
        this.redisInventoryService = redisInventoryService;
    }

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_INVENTORY_ROLLBACK,
            groupId = "flashflow-rollback-consumer-group",
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "${flashflow.kafka.enabled:true}"
    )
    public void consumeFromKafka(InventoryRollbackEvent event) {
        processRollback(event);
    }

    @EventListener
    public void handleLocalEvent(InventoryRollbackEvent event) {
        processRollback(event);
    }

    @Transactional
    public void processRollback(InventoryRollbackEvent event) {
        if (event == null || event.getProductId() == null || event.getQuantity() == null) {
            return;
        }

        String deduplicationKey = event.getOrderNumber() != null ? 
                event.getOrderNumber() : (event.getEventId() != null ? event.getEventId() : "unknown");

        if (!processedRollbacks.add(deduplicationKey)) {
            log.warn("[RollbackConsumer] Duplicate rollback detected for key: {}. Skipping to prevent over-crediting.", 
                    deduplicationKey);
            return;
        }

        Long productId = event.getProductId();
        Integer quantity = event.getQuantity();

        log.info("[RollbackConsumer] Executing stock compensation: +{} units for product ID {} [Reason: {}]", 
                quantity, productId, event.getReason());

        // 1. Atomic PostgreSQL increment
        int updated = productRepository.incrementStockAtomic(productId, quantity);

        // 2. Redis stock cache replenishment
        redisInventoryService.replenishStock(productId, quantity);

        log.info("[RollbackConsumer] Stock compensation complete for product ID {}. Database rows updated: {}", 
                productId, updated);
    }
}
