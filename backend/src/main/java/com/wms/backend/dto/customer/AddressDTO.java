package com.wms.backend.dto.customer;

import com.wms.backend.entity.CustomerAddress;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AddressDTO(
        UUID    id,
        String  label,
        String  street,
        String  city,
        String  region,
        String  country,
        Boolean isDefault
) {
    // Factory method which converts entity to DTO
    public static AddressDTO from(CustomerAddress address) {
        return new AddressDTO(
                address.getId(),
                address.getLabel(),
                address.getStreet(),
                address.getCity(),
                address.getRegion(),
                address.getCountry(),
                address.getIsDefault()
        );
    }

    public record CreateAddressRequest(
            String  label,

            @NotBlank(message = "Street is required")
            String  street,

            @NotBlank(message = "City is required")
            String  city,

            String  region,
            String  country,
            Boolean isDefault
    ) {
        public CreateAddressRequest {
            if (country   == null) country   = "Tanzania";
            if (isDefault == null) isDefault = false;
        }
    }

    public record UpdateAddressRequest(
            String  label,
            String  street,
            String  city,
            String  region,
            String  country,
            Boolean isDefault
    ) {}
}