package com.wms.backend.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

// All fields are optional for updates
// Only non-null fields will be applied
public record UpdateProductRequest(

        @Size(max = 200)
        String name,

        String description,

        UUID categoryId,

        String unitOfMeasure,

        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal costPrice,

        @DecimalMin(value = "0.01")
        BigDecimal salePrice,

        Integer reorderPoint,

        Boolean trackInventory,

        String attributes,

        String imageUrl
) {}