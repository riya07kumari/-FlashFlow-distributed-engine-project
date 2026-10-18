package com.flashflow.service;

import com.flashflow.config.KafkaTopicConfig;
import com.flashflow.event.InventoryRollbackEvent;
import com.flashflow.event.PaymentEvent;
import com.flashflow.model.Order;
import com.flashflow.model.OrderStatus;
import com.flashflow.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderRepository orderRepository;
    private final KafkaEventPublisher eventPublisher;

    public PaymentEventConsumer(OrderRepository orderRepository, KafkaEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_PAYMENTS,
            groupId = "flashflow-payment-consumer-group",
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "${flashflow.kafka.enabled:true}"
    )
    public void consumeFromKafka(PaymentEvent paymentEvent) {
        processPaymentEvent(paymentEvent);
    }

    @EventListener
    public void handleLocalEvent(PaymentEvent paymentEvent) {
        processPaymentEvent(paymentEvent);
    }

    @Transactional
    public void processPaymentEvent(PaymentEvent paymentEvent) {
        if (paymentEvent == null || paymentEvent.getOrderNumber() == null) {
            return;
        }

        String orderNumber = paymentEvent.getOrderNumber();
        log.info("[PaymentConsumer] Processing payment event for order: {} [Status: {}]", 
                orderNumber, paymentEvent.getStatus());

        Optional<Order> orderOpt = orderRepository.findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            log.warn("[PaymentConsumer] Order not found for order number: {}", orderNumber);
            return;
        }

        Order order = orderOpt.get();

        if ("SUCCESS".equalsIgnoreCase(paymentEvent.getStatus())) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentReference(paymentEvent.getPaymentId());
            orderRepository.save(order);
            log.info("[PaymentConsumer] Order {} successfully CONFIRMED via Payment {}", 
                    orderNumber, paymentEvent.getPaymentId());
        } else if ("FAILED".equalsIgnoreCase(paymentEvent.getStatus())) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            log.warn("[PaymentConsumer] Payment FAILED for order {}. Triggering inventory rollback compensating event...", 
                    orderNumber);

            // Trigger asynchronous inventory rollback
            InventoryRollbackEvent rollbackEvent = new InventoryRollbackEvent(
                    null,
                    orderNumber,
                    order.getProductId(),
                    order.getQuantity(),
                    "PAYMENT_FAILED"
            );
            eventPublisher.publishInventoryRollbackEvent(rollbackEvent);
        }
    }
}
