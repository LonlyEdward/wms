package com.wms.backend.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// All fields optional
// only non null fields are applied
public record UpdateCustomerRequest(

        @Size(max = 200)
        String name,

        @Email
        String email,

        String phone,
        String accountType,
        BigDecimal creditLimit,
        String paymentTerms,
        String notes
) {}