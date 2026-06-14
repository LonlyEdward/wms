package com.wms.backend.repository;

import com.wms.backend.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockMovementRepository
        extends JpaRepository<StockMovement, UUID> {

    // Compute current stock for a product
    // SUM of all movement quantities
    // Positive movements are IN/RELEASE, negative are OUT/RESERVATION
    @Query("""
        SELECT COALESCE(SUM(m.quantity), 0)
        FROM StockMovement m
        WHERE m.product.id = :productId
        """)
    Integer computeCurrentStock(@Param("productId") UUID productId);

    // Compute only reservation movements (negative quantities)
    // Used to calculate available stock = current - |reserved|
    @Query("""
        SELECT COALESCE(SUM(ABS(m.quantity)), 0)
        FROM StockMovement m
        WHERE m.product.id = :productId
        AND m.movementType = 'RESERVATION'
        """)
    Integer computeReservedStock(@Param("productId") UUID productId);

    // Paginated movement history for a product
    Page<StockMovement> findAllByProductIdOrderByCreatedAtDesc(
            UUID productId, Pageable pageable
    );
}