package com.wms.backend.dto.order;

import com.wms.backend.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "New status is required")
        OrderStatus newStatus,

        // Reason is required for certain transitions
        // like reversals and cancellations
        String reason
) {}