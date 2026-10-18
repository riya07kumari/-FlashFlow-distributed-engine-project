package com.flashflow.controller;

import com.flashflow.dto.ProductDto;
import com.flashflow.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "Endpoints for viewing inventory and resetting stock")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products with real-time stock levels")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single product by ID")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/{id}/reset-stock")
    @Operation(summary = "Reset product stock for repeat testing")
    public ResponseEntity<ProductDto> resetStock(@PathVariable Long id, @RequestParam(defaultValue = "10") Integer stock) {
        return ResponseEntity.ok(productService.resetProductStock(id, stock));
    }
}
