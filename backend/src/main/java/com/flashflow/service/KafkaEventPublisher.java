package com.flashflow.service;

import com.flashflow.config.KafkaTopicConfig;
import com.flashflow.event.InventoryRollbackEvent;
import com.flashflow.event.OrderEvent;
import com.flashflow.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Enterprise Event Publisher.
 * Dispatches domain events to Apache Kafka clusters.
 * Features automatic fallback to Spring ApplicationEventPublisher when running in test
 * or standalone modes without an active Kafka cluster.
 */
@Service
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher localEventPublisher;

    @org.springframework.beans.factory.annotation.Value("${flashflow.kafka.enabled:false}")
    private boolean kafkaEnabled;

    public KafkaEventPublisher(@Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate,
                               ApplicationEventPublisher localEventPublisher) {
        this.kafkaTemplate = kafkaTemplate;
        this.localEventPublisher = localEventPublisher;
    }

    public boolean isKafkaAvailable() {
        return kafkaEnabled && kafkaTemplate != null;
    }

    public void publishOrderEvent(OrderEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        if (isKafkaAvailable()) {
            try {
                kafkaTemplate.send(KafkaTopicConfig.TOPIC_ORDERS, event.getOrderNumber(), event)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("[Kafka] Failed to publish OrderEvent for order {}: {}", 
                                        event.getOrderNumber(), ex.getMessage());
                                localEventPublisher.publishEvent(event);
                            } else {
                                log.info("[Kafka] Published OrderEvent: {} to partition {}", 
                                        event.getOrderNumber(), result.getRecordMetadata().partition());
                            }
                        });
                return;
            } catch (Exception e) {
                log.warn("[Kafka] Error sending to Kafka cluster: {}. Falling back to in-process bus.", e.getMessage());
            }
        }

        log.info("[EventBus] Dispatching OrderEvent locally: {}", event.getOrderNumber());
        localEventPublisher.publishEvent(event);
    }

    public void publishPaymentEvent(PaymentEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        if (isKafkaAvailable()) {
            try {
                kafkaTemplate.send(KafkaTopicConfig.TOPIC_PAYMENTS, event.getOrderNumber(), event)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("[Kafka] Failed to publish PaymentEvent for order {}: {}", 
                                        event.getOrderNumber(), ex.getMessage());
                                localEventPublisher.publishEvent(event);
                            } else {
                                log.info("[Kafka] Published PaymentEvent: {} [Status: {}]", 
                                        event.getOrderNumber(), event.getStatus());
                            }
                        });
                return;
            } catch (Exception e) {
                log.warn("[Kafka] Error sending PaymentEvent to Kafka: {}. Falling back to in-process bus.", e.getMessage());
            }
        }

        log.info("[EventBus] Dispatching PaymentEvent locally: {}", event.getOrderNumber());
        localEventPublisher.publishEvent(event);
    }

    public void publishInventoryRollbackEvent(InventoryRollbackEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        if (isKafkaAvailable()) {
            try {
                kafkaTemplate.send(KafkaTopicConfig.TOPIC_INVENTORY_ROLLBACK, event.getOrderNumber(), event)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("[Kafka] Failed to publish InventoryRollbackEvent for order {}: {}", 
                                        event.getOrderNumber(), ex.getMessage());
                                localEventPublisher.publishEvent(event);
                            } else {
                                log.info("[Kafka] Published InventoryRollbackEvent: Product {} +{} units [Reason: {}]", 
                                        event.getProductId(), event.getQuantity(), event.getReason());
                            }
                        });
                return;
            } catch (Exception e) {
                log.warn("[Kafka] Error sending InventoryRollbackEvent: {}. Falling back to in-process bus.", e.getMessage());
            }
        }

        log.info("[EventBus] Dispatching InventoryRollbackEvent locally: Product {} +{} units", 
                event.getProductId(), event.getQuantity());
        localEventPublisher.publishEvent(event);
    }
}
