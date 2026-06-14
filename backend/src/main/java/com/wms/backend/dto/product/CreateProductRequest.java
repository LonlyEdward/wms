package com.wms.backend.dto.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(

        @NotBlank(message = "SKU is required")
        @Size(max = 100, message = "SKU must not exceed 100 characters")
        String sku,

        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        String description,

        UUID categoryId,

        UUID parentId,

        @NotBlank(message = "Unit of measure is required")
        String unitOfMeasure,

        @NotNull(message = "Cost price is required")
        @DecimalMin(value = "0.0",
                inclusive = true,
                message = "Cost price cannot be negative")
        BigDecimal costPrice,

        @NotNull(message = "Sale price is required")
        @DecimalMin(value = "0.01",
                message = "Sale price must be greater than zero")
        BigDecimal salePrice,

        @Min(value = 0, message = "Reorder point cannot be negative")
        Integer reorderPoint,

        Boolean trackInventory,

        String attributes,

        String imageUrl
) {
    // Default values for optional fields
    // Records do not support default values directly
    // so we use a compact constructor to set them
    public CreateProductRequest {
        if (reorderPoint  == null) reorderPoint  = 10;
        if (trackInventory == null) trackInventory = true;
        if (unitOfMeasure == null) unitOfMeasure = "UNIT";
    }
}