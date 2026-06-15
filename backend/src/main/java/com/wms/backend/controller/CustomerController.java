package com.wms.backend.controller;

import com.wms.backend.dto.ApiResponse;
import com.wms.backend.dto.customer.*;
import com.wms.backend.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Customers", description = "Customer account management")
public class CustomerController {

    private final CustomerService customerService;

    // Customer CRUD

    @GetMapping
    @Operation(summary = "List customers with optional search and filters")
    public ResponseEntity<ApiResponse<Page<CustomerSummaryDTO>>> getCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accountType,
            @PageableDefault(size = 20, sort = "name")
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                customerService.getCustomers(search, status, accountType, pageable)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single customer with full details")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomer(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                ApiResponse.success(customerService.getCustomerById(id))
        );
    }

    @PostMapping
    @Operation(summary = "Create a new customer account")
    public ResponseEntity<ApiResponse<CustomerDTO>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        customerService.createCustomer(request),
                        "Customer created successfully"
                ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a customer account")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                customerService.updateCustomer(id, request)
        ));
    }

    //Account hold management


    @PostMapping("/{id}/hold")
    @Operation(summary = "Place a customer account on hold")
    public ResponseEntity<ApiResponse<CustomerDTO>> holdAccount(
            @PathVariable UUID id,
            @Valid @RequestBody HoldAccountRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                customerService.holdAccount(id, request),
                "Account placed on hold"
        ));
    }

    @PostMapping("/{id}/release-hold")
    @Operation(summary = "Release a customer account from hold")
    public ResponseEntity<ApiResponse<CustomerDTO>> releaseHold(
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(
                customerService.releaseHold(id),
                "Account hold released"
        ));
    }

    //Address management


    @GetMapping("/{customerId}/addresses")
    @Operation(summary = "Get all addresses for a customer")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAddresses(
            @PathVariable UUID customerId) {

        return ResponseEntity.ok(ApiResponse.success(
                customerService.getAddresses(customerId)
        ));
    }

    @PostMapping("/{customerId}/addresses")
    @Operation(summary = "Add an address to a customer")
    public ResponseEntity<ApiResponse<AddressDTO>> addAddress(
            @PathVariable UUID customerId,
            @Valid @RequestBody AddressDTO.CreateAddressRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        customerService.addAddress(customerId, request),
                        "Address added successfully"
                ));
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    @Operation(summary = "Update a customer address")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @PathVariable UUID customerId,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressDTO.UpdateAddressRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                customerService.updateAddress(customerId, addressId, request)
        ));
    }

    @DeleteMapping("/{customerId}/addresses/{addressId}")
    @Operation(summary = "Delete a customer address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable UUID customerId,
            @PathVariable UUID addressId) {

        customerService.deleteAddress(customerId, addressId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Address deleted")
        );
    }
}