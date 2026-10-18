package com.flashflow.service;

import com.flashflow.dto.ProductDto;
import com.flashflow.exception.ResourceNotFoundException;
import com.flashflow.model.Product;
import com.flashflow.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisInventoryService redisInventoryService;

    public ProductService(ProductRepository productRepository, RedisInventoryService redisInventoryService) {
        this.productRepository = productRepository;
        this.redisInventoryService = redisInventoryService;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToDto(product);
    }

    @Transactional
    public ProductDto resetProductStock(Long id, Integer newStock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        product.setStockQuantity(newStock);
        product.setInitialStock(newStock);
        Product saved = productRepository.save(product);

        // Sync Redis cache with new stock
        redisInventoryService.syncStock(id, newStock);

        return mapToDto(saved);
    }

    private ProductDto mapToDto(Product p) {
        return new ProductDto(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getPrice(),
                p.getStockQuantity(),
                p.getInitialStock(),
                p.getImageUrl(),
                p.getCategory()
        );
    }
}
