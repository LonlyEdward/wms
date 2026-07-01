package com.wms.backend.repository;

import com.wms.backend.entity.Order;
import com.wms.backend.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndBusinessId(UUID id, UUID businessId);

    Optional<Order> findByOrderNumberAndBusinessId(
            String orderNumber, UUID businessId
    );

    @Query(value = """
        SELECT o.* FROM orders o
        JOIN customers c ON c.id = o.customer_id
        WHERE o.business_id = :businessId
        AND (CAST(:status AS TEXT) IS NULL
             OR o.status = CAST(:status AS TEXT))
        AND (CAST(:customerId AS UUID) IS NULL
             OR o.customer_id = CAST(:customerId AS UUID))
        AND (CAST(:fromDate AS TIMESTAMPTZ) IS NULL
             OR o.created_at >= CAST(:fromDate AS TIMESTAMPTZ))
        AND (CAST(:toDate AS TIMESTAMPTZ) IS NULL
             OR o.created_at <= CAST(:toDate AS TIMESTAMPTZ))
        AND (CAST(:search AS TEXT) IS NULL
             OR LOWER(o.order_number) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))
             OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))
        ORDER BY o.created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM orders o
        JOIN customers c ON c.id = o.customer_id
        WHERE o.business_id = :businessId
        AND (CAST(:status AS TEXT) IS NULL
             OR o.status = CAST(:status AS TEXT))
        AND (CAST(:customerId AS UUID) IS NULL
             OR o.customer_id = CAST(:customerId AS UUID))
        AND (CAST(:fromDate AS TIMESTAMPTZ) IS NULL
             OR o.created_at >= CAST(:fromDate AS TIMESTAMPTZ))
        AND (CAST(:toDate AS TIMESTAMPTZ) IS NULL
             OR o.created_at <= CAST(:toDate AS TIMESTAMPTZ))
        AND (CAST(:search AS TEXT) IS NULL
             OR LOWER(o.order_number) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))
             OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))
        """,
            nativeQuery = true)
    Page<Order> searchOrders(
            @Param("businessId")  UUID businessId,
            @Param("status")      String status,
            @Param("customerId")  UUID customerId,
            @Param("fromDate")    Instant fromDate,
            @Param("toDate")      Instant toDate,
            @Param("search")      String search,
            Pageable pageable
    );

    Page<Order> findAllByCustomerIdAndBusinessId(
            UUID customerId, UUID businessId, Pageable pageable
    );

    long countByBusinessIdAndStatus(UUID businessId, OrderStatus status);

    @Query(value = """
        SELECT 'ORD-' || EXTRACT(YEAR FROM NOW()) || '-'
               || LPAD(nextval('order_seq')::text, 5, '0')
        """, nativeQuery = true)
    String generateOrderNumber();
}