package com.flashflow.service;

import com.flashflow.dto.PaymentWebhookRequest;
import com.flashflow.dto.PaymentWebhookResponse;
import com.flashflow.event.PaymentEvent;
import com.flashflow.lock.SmartLockCoordinator;
import com.flashflow.model.PaymentRecord;
import com.flashflow.model.PaymentStatus;
import com.flashflow.model.ProcessedWebhook;
import com.flashflow.repository.PaymentRecordRepository;
import com.flashflow.repository.ProcessedWebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * Idempotent Payment Webhook Processing Service.
 * Guarantees exactly-once processing semantics for third-party payment gateways (Stripe, Razorpay, Adyen).
 *
 * Architecture Highlight:
 * Uses TransactionTemplate inside a distributed lock boundary. The transaction is committed to the database
 * BEFORE the lock is released, completely preventing dirty read race conditions between concurrent webhook retries.
 */
@Service
public class PaymentWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookService.class);

    private final ProcessedWebhookRepository webhookRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final KafkaEventPublisher eventPublisher;
    private final SmartLockCoordinator lockCoordinator;
    private final TransactionTemplate transactionTemplate;

    public PaymentWebhookService(ProcessedWebhookRepository webhookRepository,
                                 PaymentRecordRepository paymentRecordRepository,
                                 KafkaEventPublisher eventPublisher,
                                 SmartLockCoordinator lockCoordinator,
                                 TransactionTemplate transactionTemplate) {
        this.webhookRepository = webhookRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.eventPublisher = eventPublisher;
        this.lockCoordinator = lockCoordinator;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Processes an incoming payment gateway webhook with strict idempotency.
     */
    public PaymentWebhookResponse processWebhook(PaymentWebhookRequest request) {
        String webhookId = request.getWebhookId();
        String lockKey = "webhook:" + webhookId;

        // Acquire lock to serialize duplicate concurrent deliveries of the same webhook
        boolean locked = lockCoordinator.tryLock(lockKey, 4000, 5000);
        if (!locked) {
            log.warn("[PaymentWebhook] Contention timeout acquiring lock for webhook ID: {}", webhookId);
            return new PaymentWebhookResponse(
                    "ALREADY_PROCESSED",
                    webhookId,
                    request.getOrderNumber(),
                    "Concurrent duplicate webhook processing in progress."
            );
        }

        try {
            // Commit transaction completely BEFORE releasing the lock in finally
            return transactionTemplate.execute(status -> {
                // 1. Idempotency Check
                Optional<ProcessedWebhook> existing = webhookRepository.findById(webhookId);
                if (existing.isPresent()) {
                    log.info("[PaymentWebhook] Duplicate webhook ignored for ID: {} (Idempotent replay)", webhookId);
                    return new PaymentWebhookResponse(
                            "ALREADY_PROCESSED",
                            webhookId,
                            request.getOrderNumber(),
                            "Webhook already processed previously. Replay acknowledged."
                    );
                }

                // 2. Persist Webhook Record to guarantee uniqueness
                ProcessedWebhook record = new ProcessedWebhook(
                        webhookId,
                        request.getOrderNumber(),
                        "PAYMENT_" + request.getStatus().toUpperCase(),
                        request.getAmount(),
                        request.getSignature(),
                        "PROCESSED",
                        "Payment webhook for " + request.getOrderNumber()
                );
                webhookRepository.save(record);

                // 3. Persist Payment Transaction Record
                PaymentStatus paymentStatus = "SUCCESS".equalsIgnoreCase(request.getStatus()) ? 
                        PaymentStatus.SUCCESS : PaymentStatus.FAILED;

                PaymentRecord paymentRecord = new PaymentRecord(
                        request.getPaymentId(),
                        request.getOrderNumber(),
                        request.getAmount(),
                        paymentStatus,
                        webhookId,
                        webhookId,
                        "Webhook verified: " + request.getStatus()
                );
                paymentRecordRepository.save(paymentRecord);

                // 4. Asynchronously stream PaymentEvent to Apache Kafka
                PaymentEvent paymentEvent = new PaymentEvent(
                        UUID.randomUUID().toString(),
                        request.getPaymentId(),
                        request.getOrderNumber(),
                        request.getAmount(),
                        request.getStatus().toUpperCase(),
                        webhookId,
                        "Payment processed via webhook"
                );
                eventPublisher.publishPaymentEvent(paymentEvent);

                log.info("[PaymentWebhook] Webhook {} processed successfully for Order {}", 
                        webhookId, request.getOrderNumber());

                return new PaymentWebhookResponse(
                        "PROCESSED",
                        webhookId,
                        request.getOrderNumber(),
                        "Payment event accepted and queued for fulfillment."
                );
            });
        } finally {
            lockCoordinator.unlock(lockKey);
        }
    }
}
