package com.wms.backend.service;

import com.wms.backend.dto.customer.*;
import com.wms.backend.entity.Customer;
import com.wms.backend.entity.CustomerAddress;
import com.wms.backend.exception.AccountOnHoldException;
import com.wms.backend.exception.AppException;
import com.wms.backend.exception.BusinessRuleException;
import com.wms.backend.exception.CreditLimitExceededException;
import com.wms.backend.exception.EntityNotFoundException;
import com.wms.backend.repository.CustomerAddressRepository;
import com.wms.backend.repository.CustomerRepository;
import com.wms.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository        customerRepository;
    private final CustomerAddressRepository addressRepository;

    // Customer CRUD

    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTS')")
    @Transactional(readOnly = true)
    public Page<CustomerSummaryDTO> getCustomers(String search,
                                                 String status,
                                                 String accountType,
                                                 Pageable pageable) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        return customerRepository
                .searchCustomers(businessId, search, status, accountType, pageable)
                .map(customer -> CustomerSummaryDTO.from(
                        customer,
                        getOutstandingBalance(customer.getId())
                ));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTS')")
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(UUID id) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Customer customer = customerRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer", id)
                );

        return CustomerDTO.from(
                customer,
                getOutstandingBalance(id)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CustomerDTO createCustomer(CreateCustomerRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        // Prevent duplicate emails within the same business
        if (customerRepository.existsByEmailAndBusinessId(
                request.email(), businessId)) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "DUPLICATE_EMAIL",
                    "A customer with email '" + request.email()
                            + "' already exists"
            );
        }

        Customer customer = Customer.builder()
                .businessId(businessId)
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .accountType(request.accountType())
                .status("ACTIVE")
                .creditLimit(request.creditLimit())
                .paymentTerms(request.paymentTerms())
                .notes(request.notes())
                .build();

        Customer saved = customerRepository.save(customer);

        // Create addresses if provided in the request
        if (request.addresses() != null && !request.addresses().isEmpty()) {
            for (AddressDTO.CreateAddressRequest addr
                    : request.addresses()) {
                createAddressForCustomer(saved, addr);
            }
        }

        log.info("Customer created: {} ({})", saved.getName(), saved.getEmail());

        return CustomerDTO.from(saved, BigDecimal.ZERO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CustomerDTO updateCustomer(UUID id,
                                      UpdateCustomerRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Customer customer = customerRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer", id)
                );

        // Check the new email is not already taken by another customer
        if (request.email() != null
                && !request.email().equals(customer.getEmail())
                && customerRepository.existsByEmailAndBusinessIdAndIdNot(
                request.email(), businessId, id)) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "DUPLICATE_EMAIL",
                    "Email '" + request.email()
                            + "' is already used by another customer"
            );
        }

        // Apply only nonnull fields
        // null means "do not change this field"
        if (request.name()         != null) customer.setName(request.name());
        if (request.email()        != null) customer.setEmail(request.email());
        if (request.phone()        != null) customer.setPhone(request.phone());
        if (request.accountType()  != null) customer.setAccountType(request.accountType());
        if (request.creditLimit()  != null) customer.setCreditLimit(request.creditLimit());
        if (request.paymentTerms() != null) customer.setPaymentTerms(request.paymentTerms());
        if (request.notes()        != null) customer.setNotes(request.notes());

        Customer saved = customerRepository.save(customer);

        return CustomerDTO.from(saved, getOutstandingBalance(id));
    }

    // Account hold management

    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTS')")
    @Transactional
    public CustomerDTO holdAccount(UUID id, HoldAccountRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Customer customer = customerRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer", id)
                );

        if ("ON_HOLD".equals(customer.getStatus())) {
            throw new BusinessRuleException(
                    "ALREADY_ON_HOLD",
                    "Customer account is already on hold"
            );
        }

        customer.setStatus("ON_HOLD");
        // Store the hold reason in the notes field
        String holdNote = "[HOLD] " + request.reason();
        customer.setNotes(holdNote);

        Customer saved = customerRepository.save(customer);
        log.warn("Customer account placed on hold: {} — Reason: {}",
                customer.getName(), request.reason());

        return CustomerDTO.from(saved, getOutstandingBalance(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTS')")
    @Transactional
    public CustomerDTO releaseHold(UUID id) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Customer customer = customerRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer", id)
                );

        if (!"ON_HOLD".equals(customer.getStatus())) {
            throw new BusinessRuleException(
                    "NOT_ON_HOLD",
                    "Customer account is not on hold"
            );
        }

        customer.setStatus("ACTIVE");
        Customer saved = customerRepository.save(customer);
        log.info("Customer account hold released: {}", customer.getName());

        return CustomerDTO.from(saved, getOutstandingBalance(id));
    }

    //Credit limit checking

    // Called by OrderService before creating an order
    //central enforcement point for credit limits
    @Transactional(readOnly = true)
    public void checkCreditLimit(UUID customerId, BigDecimal orderAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer", customerId)
                );

        // If customer is on hold, block all new orders immediately
        if ("ON_HOLD".equals(customer.getStatus())) {
            throw new AccountOnHoldException(customer.getName());
        }

        // If credit limit is zero, it means unlimited credit
        BigDecimal limit = customer.getCreditLimit();
        if (limit == null || limit.compareTo(BigDecimal.ZERO) == 0) {
            return; // No limit set — allow the order
        }

        BigDecimal outstanding = getOutstandingBalance(customerId);

        // Check if adding this order would exceed the credit limit
        if (outstanding.add(orderAmount).compareTo(limit) > 0) {
            throw new CreditLimitExceededException(
                    orderAmount, outstanding, limit
            );
        }
    }

    // Address management

    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTS')")
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddresses(UUID customerId) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        // Verify customer belongs to this business before returning addresses
        customerRepository.findByIdAndBusinessId(customerId, businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer", customerId)
                );

        return addressRepository
                .findAllByCustomerIdOrderByIsDefaultDesc(customerId)
                .stream()
                .map(AddressDTO::from)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AddressDTO addAddress(UUID customerId,
                                 AddressDTO.CreateAddressRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Customer customer = customerRepository
                .findByIdAndBusinessId(customerId, businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer", customerId)
                );

        return AddressDTO.from(createAddressForCustomer(customer, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AddressDTO updateAddress(UUID customerId,
                                    UUID addressId,
                                    AddressDTO.UpdateAddressRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        // Verify customer belongs to business
        customerRepository.findByIdAndBusinessId(customerId, businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer", customerId)
                );

        CustomerAddress address = addressRepository
                .findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Address", addressId)
                );

        if (request.label()     != null) address.setLabel(request.label());
        if (request.street()    != null) address.setStreet(request.street());
        if (request.city()      != null) address.setCity(request.city());
        if (request.region()    != null) address.setRegion(request.region());
        if (request.country()   != null) address.setCountry(request.country());

        // If setting this address as default, clear all others first
        if (Boolean.TRUE.equals(request.isDefault())) {
            addressRepository.clearDefaultForCustomer(customerId);
            address.setIsDefault(true);
        }

        return AddressDTO.from(addressRepository.save(address));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteAddress(UUID customerId, UUID addressId) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        customerRepository.findByIdAndBusinessId(customerId, businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer", customerId)
                );

        CustomerAddress address = addressRepository
                .findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Address", addressId)
                );

        // Prevent deleting the only address
        List<CustomerAddress> allAddresses = addressRepository
                .findAllByCustomerIdOrderByIsDefaultDesc(customerId);

        if (allAddresses.size() == 1) {
            throw new BusinessRuleException(
                    "CANNOT_DELETE_ONLY_ADDRESS",
                    "Cannot delete the only address. "
                            + "Add another address first."
            );
        }

        // If deleting the default, make the next one default
        if (address.getIsDefault() && allAddresses.size() > 1) {
            CustomerAddress nextDefault = allAddresses.stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .findFirst()
                    .orElse(null);

            if (nextDefault != null) {
                nextDefault.setIsDefault(true);
                addressRepository.save(nextDefault);
            }
        }

        addressRepository.delete(address);
    }

    // Internal helpers
    // Shared helper for creating an address inside a transaction
    // Used by both createCustomer and addAddress
    private CustomerAddress createAddressForCustomer(
            Customer customer,
            AddressDTO.CreateAddressRequest request) {

        // If this is the first address or marked as default,
        // clear other defaults first
        if (Boolean.TRUE.equals(request.isDefault())) {
            addressRepository.clearDefaultForCustomer(customer.getId());
        }

        // If no addresses exist yet, make this one default automatically
        List<CustomerAddress> existing = addressRepository
                .findAllByCustomerIdOrderByIsDefaultDesc(customer.getId());
        boolean shouldBeDefault = existing.isEmpty()
                || Boolean.TRUE.equals(request.isDefault());

        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .businessId(customer.getBusinessId())
                .label(request.label())
                .street(request.street())
                .city(request.city())
                .region(request.region())
                .country(request.country() != null
                        ? request.country() : "Tanzania")
                .isDefault(shouldBeDefault)
                .build();

        return addressRepository.save(address);
    }

    // Calculate the total outstanding balance for a customer
    public BigDecimal getOutstandingBalance(UUID customerId) {
        BigDecimal balance =
                customerRepository.calculateOutstandingBalance(customerId);
        return balance != null ? balance : BigDecimal.ZERO;
    }
}