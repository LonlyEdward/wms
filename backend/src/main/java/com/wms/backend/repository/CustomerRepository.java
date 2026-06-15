package com.wms.backend.repository;

import com.wms.backend.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    // Find customer scoped to a business
    Optional<Customer> findByIdAndBusinessId(UUID id, UUID businessId);

    // Find customer linked to a specific portal user
    Optional<Customer> findByUserIdAndBusinessId(UUID userId, UUID businessId);

    // Search customers with optional filters
    // status filter is optional, null means return all statuses
    @Query("""
        SELECT c FROM Customer c
        WHERE c.businessId = :businessId
        AND (:search IS NULL
             OR LOWER(c.name)  LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(c.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        AND (:status IS NULL OR c.status = :status)
        AND (:accountType IS NULL OR c.accountType = :accountType)
        ORDER BY c.name ASC
        """)
    Page<Customer> searchCustomers(
            @Param("businessId")  UUID businessId,
            @Param("search")      String search,
            @Param("status")      String status,
            @Param("accountType") String accountType,
            Pageable pageable
    );

    // Check if email is already used by another customer in this business
    boolean existsByEmailAndBusinessId(String email, UUID businessId);

    // Check if email belongs to a different customer (used during update)
    boolean existsByEmailAndBusinessIdAndIdNot(
            String email, UUID businessId, UUID id
    );

    // Calculate total outstanding balance across all unpaid invoices
    @Query(value = """
        SELECT COALESCE(SUM(i.amount_outstanding), 0)
        FROM invoices i
        WHERE i.customer_id = :customerId
        AND i.status NOT IN ('PAID', 'VOIDED')
        """, nativeQuery = true)
    BigDecimal calculateOutstandingBalance(
            @Param("customerId") UUID customerId
    );
}