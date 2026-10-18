package com.flashflow.controller;

import com.flashflow.dto.PaymentWebhookRequest;
import com.flashflow.dto.PaymentWebhookResponse;
import com.flashflow.service.PaymentWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Webhook Controller", description = "Asynchronous idempotent payment gateway callback integration")
public class PaymentWebhookController {

    private final PaymentWebhookService webhookService;

    public PaymentWebhookController(PaymentWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/webhook")
    @Operation(summary = "Idempotent Payment Webhook", description = "Receives payment notifications and emits asynchronous Kafka events")
    public ResponseEntity<PaymentWebhookResponse> handleWebhook(
            @RequestHeader(value = "X-Webhook-Id", required = false) String headerWebhookId,
            @Valid @RequestBody PaymentWebhookRequest request) {

        if (headerWebhookId != null && !headerWebhookId.trim().isEmpty() && 
                (request.getWebhookId() == null || request.getWebhookId().trim().isEmpty())) {
            request.setWebhookId(headerWebhookId);
        }

        PaymentWebhookResponse response = webhookService.processWebhook(request);
        return ResponseEntity.ok(response);
    }
}
