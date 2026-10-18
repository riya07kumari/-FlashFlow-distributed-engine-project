package com.flashflow.controller;

import com.flashflow.dto.OrderRequest;
import com.flashflow.dto.OrderResponse;
import com.flashflow.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Processing", description = "High-concurrency flash-sale checkout endpoints")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/flash-buy")
    @Operation(summary = "Place a flash-sale purchase with idempotency, distributed locking, and zero overselling protection")
    public ResponseEntity<OrderResponse> placeFlashSaleOrder(
            @Valid @RequestBody OrderRequest request,
            @Parameter(description = "Client-provided unique idempotency key to prevent double charging")
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        OrderResponse response = orderService.createFlashSaleOrder(request, idempotencyKey);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/checkout-async")
    @Operation(summary = "Reserve inventory and place order in PENDING_PAYMENT state for asynchronous gateway processing")
    public ResponseEntity<OrderResponse> placeAsyncOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

        OrderResponse response = orderService.createPendingOrder(request, idempotencyKey);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get 20 most recent orders for live audit feed")
    public ResponseEntity<List<OrderResponse>> getRecentOrders() {
        return ResponseEntity.ok(orderService.getRecentOrders());
    }
}
