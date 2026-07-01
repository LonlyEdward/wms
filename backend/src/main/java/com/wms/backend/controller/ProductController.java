package com.wms.backend.controller;

import com.wms.backend.dto.ApiResponse;
import com.wms.backend.dto.product.*;
import com.wms.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Products", description = "Product catalogue and inventory management")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List products with optional search and filter")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @PageableDefault(size = 20, sort = "name")
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getProducts(search, categoryId, pageable)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single product with stock levels")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                ApiResponse.success(productService.getProductById(id))
        );
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        productService.createProduct(request),
                        "Product created successfully"
                ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.updateProduct(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive a product (soft delete)")
    public ResponseEntity<ApiResponse<Void>> archiveProduct(
            @PathVariable UUID id) {

        productService.archiveProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Product archived successfully")
        );
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get products at or below reorder point")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getLowStock() {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getLowStockProducts()
        ));
    }

    //Stock endpoints

    @PostMapping("/stock/adjust")
    @Operation(summary = "Manually adjust stock for a product")
    public ResponseEntity<ApiResponse<StockMovementDTO>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.adjustStock(request),
                "Stock adjusted successfully"
        ));
    }

    @GetMapping("/{id}/stock/movements")
    @Operation(summary = "Get stock movement history for a product")
    public ResponseEntity<ApiResponse<Page<StockMovementDTO>>> getMovements(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getStockMovements(id, pageable)
        ));
    }

    // Category endpoints

    @GetMapping("/categories")
    @Operation(summary = "List all product categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategories() {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getCategories()
        ));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a new product category")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @Valid @RequestBody CategoryDTO.CreateCategoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        productService.createCategory(request),
                        "Category created successfully"
                ));
    }
}