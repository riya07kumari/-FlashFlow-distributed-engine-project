package com.flashflow.controller;

import com.flashflow.dto.PaymentReconciliationResponse;
import com.flashflow.service.PaymentReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reconciliation")
@Tag(name = "Payment Reconciliation Controller", description = "Admin & operational management of payment reconciliation and inventory recovery")
public class PaymentReconciliationController {

    private final PaymentReconciliationService reconciliationService;

    public PaymentReconciliationController(PaymentReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/run")
    @Operation(summary = "Trigger Payment Reconciliation", description = "Sweeps pending payment orders older than timeout and returns reserved stock via Kafka")
    public ResponseEntity<PaymentReconciliationResponse> triggerReconciliation(
            @RequestParam(defaultValue = "2") int timeoutMinutes) {

        PaymentReconciliationResponse response = reconciliationService.runReconciliation(timeoutMinutes);
        return ResponseEntity.ok(response);
    }
}
