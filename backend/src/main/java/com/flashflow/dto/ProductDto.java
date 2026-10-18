package com.flashflow.dto;

import java.math.BigDecimal;

public class ProductDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer initialStock;
    private String imageUrl;
    private String category;
    private Integer percentageLeft;

    public ProductDto() {}

    public ProductDto(Long id, String title, String description, BigDecimal price, Integer stockQuantity, 
                      Integer initialStock, String imageUrl, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.initialStock = initialStock;
        this.imageUrl = imageUrl;
        this.category = category;
        if (initialStock != null && initialStock > 0) {
            this.percentageLeft = (int) Math.round(((double) stockQuantity / initialStock) * 100);
        } else {
            this.percentageLeft = 0;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Integer getInitialStock() { return initialStock; }
    public void setInitialStock(Integer initialStock) { this.initialStock = initialStock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPercentageLeft() { return percentageLeft; }
    public void setPercentageLeft(Integer percentageLeft) { this.percentageLeft = percentageLeft; }
}
