package com.wms.backend.repository;

import com.wms.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // Find product by ID scoped to a business
    // The businessId check prevents one business seeing another business products
    Optional<Product> findByIdAndBusinessId(UUID id, UUID businessId);

    // Search products with optional filters
    // All parameters are nullable
    // if null the filter is skipped
    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND p.isActive = true AND p.parent IS NULL " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CAST(:search AS string)) OR LOWER(p.sku) LIKE LOWER(CAST(:search AS string))) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> searchProducts(
            @Param("businessId") UUID businessId,
            @Param("search") String search,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );

    // Find all variants of a parent product
    List<Product> findAllByParentIdAndIsActiveTrue(UUID parentId);

    // Check if a SKU already exists for this business
    boolean existsBySkuAndBusinessId(String sku, UUID businessId);

    // Check if a SKU exists for a different product (used during update)
    boolean existsBySkuAndBusinessIdAndIdNot(
            String sku, UUID businessId, UUID id
    );

    // Query that finds products whose computed stock is at or below their reorder point.
    @Query(value = """
        SELECT p.* FROM products p
        WHERE p.business_id = :businessId
        AND p.is_active = true
        AND p.track_inventory = true
        AND p.parent_id IS NULL
        AND (
            SELECT COALESCE(SUM(sm.quantity), 0)
            FROM stock_movements sm
            WHERE sm.product_id = p.id
        ) <= p.reorder_point
        """, nativeQuery = true)
    List<Product> findLowStockProducts(@Param("businessId") UUID businessId);
}