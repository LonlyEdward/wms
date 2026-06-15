package com.wms.backend.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        UUID deliveryAddressId,

        // STANDARD or BULK
        String orderType,

        String notes,

        @NotEmpty(message = "Order must have at least one item")
        @Valid
        List<OrderItemRequest> items
) {
    public CreateOrderRequest {
        if (orderType == null) orderType = "STANDARD";
    }

    // Nested record for each line item in the order
    public record OrderItemRequest(

            @NotNull(message = "Product ID is required")
            UUID productId,

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            Integer quantity,

            // Optional price override
            // used by pricing engine
            // If null the product default price is used
            BigDecimal unitPriceOverride,

            String notes
    ) {}
}