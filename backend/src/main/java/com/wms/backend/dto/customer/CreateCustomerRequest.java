package com.wms.backend.dto.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CreateCustomerRequest(

        @NotBlank(message = "Customer name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        @Size(max = 30)
        String phone,

        // RETAIL, WHOLESALE, DISTRIBUTOR
        String accountType,

        @DecimalMin(value = "0.0",
                message = "Credit limit cannot be negative")
        BigDecimal creditLimit,

        // IMMEDIATE, NET_7, NET_14, NET_30, NET_60
        String paymentTerms,

        String notes,

        // Optional to create addresses at the same time as the customer
        @Valid
        List<AddressDTO.CreateAddressRequest> addresses
) {
    public CreateCustomerRequest {
        if (accountType  == null) accountType  = "RETAIL";
        if (creditLimit  == null) creditLimit  = BigDecimal.ZERO;
        if (paymentTerms == null) paymentTerms = "NET_30";
    }
}