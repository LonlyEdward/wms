package com.wms.backend.repository;

import com.wms.backend.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAddressRepository
        extends JpaRepository<CustomerAddress, UUID> {

    List<CustomerAddress> findAllByCustomerIdOrderByIsDefaultDesc(
            UUID customerId
    );

    Optional<CustomerAddress> findByIdAndCustomerId(
            UUID id, UUID customerId
    );

    // Before setting a new default address, clear all existing defaults
    // for this customer so only one can be default at a time
    @Modifying
    @Query("""
        UPDATE CustomerAddress a
        SET a.isDefault = false
        WHERE a.customer.id = :customerId
        """)
    void clearDefaultForCustomer(@Param("customerId") UUID customerId);
}