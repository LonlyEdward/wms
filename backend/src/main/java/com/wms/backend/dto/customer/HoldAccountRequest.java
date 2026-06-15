package com.wms.backend.dto.customer;

import jakarta.validation.constraints.NotBlank;

public record HoldAccountRequest(

        @NotBlank(message = "A reason is required when placing an account on hold")
        String reason
) {}