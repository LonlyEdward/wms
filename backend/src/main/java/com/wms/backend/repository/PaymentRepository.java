package com.wms.backend.repository;

import com.wms.backend.entity.Payment;
import com.wms.backend.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByInvoiceIdOrderByCreatedAtAsc(UUID invoiceId);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.invoice.id = :invoiceId
        AND p.status = 'COMPLETED'
        """)
    BigDecimal sumCompletedPayments(@Param("invoiceId") UUID invoiceId);
}