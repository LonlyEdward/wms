package com.wms.backend.dto.product;

import com.wms.backend.entity.ProductCategory;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CategoryDTO(
        UUID id,
        String name,
        UUID parentId,
        String parentName
) {
    public static CategoryDTO from(ProductCategory category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getParent() != null
                        ? category.getParent().getId()
                        : null,
                category.getParent() != null
                        ? category.getParent().getName()
                        : null
        );
    }

    // Request record for creating a category
    // Defined as a nested record to keep related things together
    public record CreateCategoryRequest(
            @NotBlank(message = "Category name is required")
            String name,
            UUID parentId
    ) {}
}