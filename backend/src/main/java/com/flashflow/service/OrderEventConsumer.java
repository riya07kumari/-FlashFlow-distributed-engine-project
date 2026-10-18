package com.flashflow.service;

import com.flashflow.config.KafkaTopicConfig;
import com.flashflow.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_ORDERS,
            groupId = "flashflow-order-audit-group",
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "${flashflow.kafka.enabled:true}"
    )
    public void consumeFromKafka(OrderEvent event) {
        logOrderEvent(event);
    }

    @EventListener
    public void handleLocalEvent(OrderEvent event) {
        logOrderEvent(event);
    }

    private void logOrderEvent(OrderEvent event) {
        if (event == null) return;
        log.info("[OrderAudit] Order event received: Order {} | Product ID: {} | Qty: {} | Total: ${} | Status: {}",
                event.getOrderNumber(), event.getProductId(), event.getQuantity(), 
                event.getTotalAmount(), event.getStatus());
    }
}
