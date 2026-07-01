package com.wms.backend.dto.product;

import com.wms.backend.entity.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Summary DTO used in list responses
// Does not include variants or stock movements
public record ProductDTO(
        UUID id,
        String sku,
        String name,
        String description,
        CategoryDTO category,
        String unitOfMeasure,
        BigDecimal costPrice,
        BigDecimal salePrice,
        Integer reorderPoint,
        Boolean trackInventory,
        Boolean isActive,
        String attributes,
        String imageUrl,
        // Stock fields are populated by StockService not from the entity directly
        Integer currentStock,
        Integer reservedStock,
        Integer availableStock,
        Instant createdAt
) {
    // Nested record for category info
    // avoids a separate CategoryDTO import
    public record CategoryDTO(UUID id, String name) {}

    // Factory method that converts entity to DTO
    // Stock values are passed in because they come from StockService
    public static ProductDTO from(Product product,
                                  int currentStock,
                                  int reservedStock) {
        return new ProductDTO(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory() != null
                        ? new CategoryDTO(
                        product.getCategory().getId(),
                        product.getCategory().getName())
                        : null,
                product.getUnitOfMeasure(),
                product.getCostPrice(),
                product.getSalePrice(),
                product.getReorderPoint(),
                product.getTrackInventory(),
                product.getIsActive(),
                product.getAttributes(),
                product.getImageUrl(),
                currentStock,
                reservedStock,
                currentStock - reservedStock,
                product.getCreatedAt()
        );
    }
}