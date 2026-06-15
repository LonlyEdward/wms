package com.wms.backend.dto.customer;

import com.wms.backend.entity.Customer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Full customer response including addresses and financial summary
public record CustomerDTO(
        UUID            id,
        String          name,
        String          email,
        String          phone,
        String          accountType,
        String          status,
        BigDecimal      creditLimit,
        BigDecimal      outstandingBalance,
        BigDecimal      availableCredit,
        String          paymentTerms,
        List<AddressDTO> addresses,
        Instant         createdAt
) {
    // Factory method
    // outstandingBalance passed in from the repository query
    public static CustomerDTO from(Customer customer,
                                   BigDecimal outstandingBalance) {
        BigDecimal available = customer.getCreditLimit() != null
                ? customer.getCreditLimit().subtract(outstandingBalance)
                : BigDecimal.ZERO;

        return new CustomerDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAccountType(),
                customer.getStatus(),
                customer.getCreditLimit(),
                outstandingBalance,
                available,
                customer.getPaymentTerms(),
                customer.getAddresses().stream()
                        .map(AddressDTO::from)
                        .toList(),
                customer.getCreatedAt()
        );
    }
}