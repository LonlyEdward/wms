package com.wms.backend.dto.order;

import jakarta.validation.constraints.NotBlank;

public record FlagOrderRequest(

        @NotBlank(message = "A reason is required when flagging an order")
        String reason
) {}