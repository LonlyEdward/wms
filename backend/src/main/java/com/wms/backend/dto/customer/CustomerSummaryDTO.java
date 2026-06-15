package com.wms.backend.dto.customer;

import com.wms.backend.entity.Customer;

import java.math.BigDecimal;
import java.util.UUID;

// Lightweight DTO used in paginated list responses
// Does not include addresses or full financial details
// Keeps list responses fast by avoiding loading address collections
public record CustomerSummaryDTO(
        UUID       id,
        String     name,
        String     email,
        String     phone,
        String     accountType,
        String     status,
        BigDecimal creditLimit,
        BigDecimal outstandingBalance,
        String     paymentTerms
) {
    public static CustomerSummaryDTO from(Customer customer,
                                          BigDecimal outstanding) {
        return new CustomerSummaryDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAccountType(),
                customer.getStatus(),
                customer.getCreditLimit(),
                outstanding,
                customer.getPaymentTerms()
        );
    }
}