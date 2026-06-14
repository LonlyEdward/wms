package com.wms.backend.dto.product;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StockAdjustmentRequest(

        @NotNull(message = "Product ID is required")
        UUID productId,

        @NotNull(message = "Quantity is required")
        Integer quantity,  // positive = add stock, negative = remove stock

        String reason      // required when quantity is negative
) {
    // Validate that a reason is provided when removing stock
    public StockAdjustmentRequest {
        if (quantity != null && quantity < 0
                && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException(
                    "A reason is required when removing stock"
            );
        }
    }
}