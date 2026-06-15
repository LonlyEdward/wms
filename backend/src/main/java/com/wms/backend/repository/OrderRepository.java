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

    // Find order scoped to a business
    Optional<Order> findByIdAndBusinessId(UUID id, UUID businessId);

    // Find order by order number scoped to a business
    Optional<Order> findByOrderNumberAndBusinessId(
            String orderNumber, UUID businessId
    );

    // Search orders with optional filters
    // All filter parameters are optional
    // null means skip that filter
    @Query("""
        SELECT o FROM Order o
        WHERE o.businessId = :businessId
        AND (:status IS NULL OR o.status = :status)
        AND (:customerId IS NULL OR o.customer.id = :customerId)
        AND (:from IS NULL OR o.createdAt >= :from)
        AND (:to IS NULL OR o.createdAt <= :to)
        AND (:search IS NULL
             OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%',:search,'%'))
             OR LOWER(o.customer.name) LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY o.createdAt DESC
        """)
    Page<Order> searchOrders(
            @Param("businessId") UUID businessId,
            @Param("status")     OrderStatus status,
            @Param("customerId") UUID customerId,
            @Param("from")       Instant from,
            @Param("to")         Instant to,
            @Param("search")     String search,
            Pageable pageable
    );

    // Orders for a specific customer
    // used in portal and customer profile
    Page<Order> findAllByCustomerIdAndBusinessId(
            UUID customerId, UUID businessId, Pageable pageable
    );

    // Count orders by status
    // used for dashboard KPIs
    long countByBusinessIdAndStatus(UUID businessId, OrderStatus status);

    // Generate the next order number using the database sequence
    // Format: ORD-2026-00001
    @Query(value = """
        SELECT 'ORD-' || EXTRACT(YEAR FROM NOW()) || '-'
               || LPAD(nextval('order_seq')::text, 5, '0')
        """, nativeQuery = true)
    String generateOrderNumber();
}