package com.wms.backend.repository;

import com.wms.backend.entity.Invoice;
import com.wms.backend.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByIdAndBusinessId(UUID id, UUID businessId);

    Optional<Invoice> findByOrderIdAndBusinessId(UUID orderId, UUID businessId);

    boolean existsByOrderIdAndStatusNot(UUID orderId, InvoiceStatus status);

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.businessId = :businessId
        AND (:status IS NULL OR i.status = :status)
        AND (:customerId IS NULL OR i.customer.id = :customerId)
        AND (:from IS NULL OR i.issueDate >= :from)
        AND (:to IS NULL OR i.issueDate <= :to)
        ORDER BY i.issueDate DESC
        """)
    Page<Invoice> searchInvoices(
            @Param("businessId") UUID businessId,
            @Param("status")     InvoiceStatus status,
            @Param("customerId") UUID customerId,
            @Param("from")       LocalDate from,
            @Param("to")         LocalDate to,
            Pageable pageable
    );

    Page<Invoice> findAllByCustomerIdAndBusinessId(
            UUID customerId, UUID businessId, Pageable pageable
    );

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.businessId = :businessId
        AND i.dueDate < :today
        AND i.status IN ('UNPAID', 'PARTIAL')
        """)
    List<Invoice> findOverdueInvoices(
            @Param("businessId") UUID businessId,
            @Param("today")      LocalDate today
    );

    @Modifying
    @Query("""
        UPDATE Invoice i SET i.status = 'OVERDUE'
        WHERE i.businessId = :businessId
        AND i.dueDate < :today
        AND i.status IN ('UNPAID', 'PARTIAL')
        """)
    int markOverdue(
            @Param("businessId") UUID businessId,
            @Param("today")      LocalDate today
    );

    @Query(value = """
        SELECT 'INV-' || EXTRACT(YEAR FROM NOW()) || '-'
               || LPAD(nextval('invoice_seq')::text, 5, '0')
        """, nativeQuery = true)
    String generateInvoiceNumber();

    @Query(value = """
        SELECT 'RMA-' || EXTRACT(YEAR FROM NOW()) || '-'
               || LPAD(nextval('rma_seq')::text, 5, '0')
        """, nativeQuery = true)
    String generateRmaNumber();
}