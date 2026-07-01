package com.wms.backend.repository;

import com.wms.backend.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, UUID> {

    // All top level categories for a business (no parent)
    List<ProductCategory> findAllByBusinessIdAndParentIsNull(UUID businessId);

    // All categories for a business including sub categories
    List<ProductCategory> findAllByBusinessId(UUID businessId);

    // Find by ID scoped to business
    Optional<ProductCategory> findByIdAndBusinessId(UUID id, UUID businessId);

    // Check name uniqueness within the same parent
    boolean existsByNameAndBusinessIdAndParentId(
            String name, UUID businessId, UUID parentId
    );
}