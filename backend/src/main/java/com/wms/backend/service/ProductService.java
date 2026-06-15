package com.wms.backend.service;

import com.wms.backend.dto.product.*;
import com.wms.backend.entity.Product;
import com.wms.backend.entity.ProductCategory;
import com.wms.backend.exception.AppException;
import com.wms.backend.exception.BusinessRuleException;
import com.wms.backend.exception.EntityNotFoundException;
import com.wms.backend.repository.ProductCategoryRepository;
import com.wms.backend.repository.ProductRepository;
import com.wms.backend.repository.StockMovementRepository;
import com.wms.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository         productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final StockMovementRepository   movementRepository;
    private final StockService              stockService;

    // ── Products ──────────────────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE', 'ACCOUNTS')")
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProducts(String search,
                                        UUID categoryId,
                                        Pageable pageable) {

        UUID businessId = SecurityUtils.getCurrentBusinessId();

        String searchParam = null;
        if (search != null && !search.trim().isEmpty()) {
            searchParam = "%" + search.trim() + "%";
        }

        return productRepository
                .searchProducts(businessId, searchParam, categoryId, pageable)
                .map(product -> ProductDTO.from(
                        product,
                        stockService.getCurrentStock(product.getId()),
                        stockService.getReservedStock(product.getId())
                ));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE', 'ACCOUNTS')")
    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID id) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Product product = productRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Product", id)
                );

        return ProductDTO.from(
                product,
                stockService.getCurrentStock(id),
                stockService.getReservedStock(id)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        // Check SKU uniqueness within the business
        if (productRepository.existsBySkuAndBusinessId(
                request.sku(), businessId)) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "DUPLICATE_SKU",
                    "A product with SKU '" + request.sku()
                            + "' already exists"
            );
        }

        Product product = Product.builder()
                .businessId(businessId)
                .sku(request.sku().toUpperCase().trim())
                .name(request.name())
                .description(request.description())
                .unitOfMeasure(request.unitOfMeasure())
                .costPrice(request.costPrice())
                .salePrice(request.salePrice())
                .reorderPoint(request.reorderPoint())
                .trackInventory(request.trackInventory())
                .attributes(request.attributes())
                .imageUrl(request.imageUrl())
                .isActive(true)
                .build();

        // Set category if provided
        if (request.categoryId() != null) {
            ProductCategory category = categoryRepository
                    .findByIdAndBusinessId(request.categoryId(), businessId)
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Category", request.categoryId()
                            )
                    );
            product.setCategory(category);
        }

        // Set parent product if this is a variant
        if (request.parentId() != null) {
            Product parent = productRepository
                    .findByIdAndBusinessId(request.parentId(), businessId)
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Parent product", request.parentId()
                            )
                    );
            product.setParent(parent);
        }

        Product saved = productRepository.save(product);
        log.info("Product created: {} ({})", saved.getName(), saved.getSku());

        // New product starts with zero stock
        return ProductDTO.from(saved, 0, 0);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ProductDTO updateProduct(UUID id, UpdateProductRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Product product = productRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Product", id));

        // Apply only the non-null fields from the request
        if (request.name()          != null) product.setName(request.name());
        if (request.description()   != null) product.setDescription(request.description());
        if (request.unitOfMeasure() != null) product.setUnitOfMeasure(request.unitOfMeasure());
        if (request.costPrice()     != null) product.setCostPrice(request.costPrice());
        if (request.salePrice()     != null) product.setSalePrice(request.salePrice());
        if (request.reorderPoint()  != null) product.setReorderPoint(request.reorderPoint());
        if (request.trackInventory()!= null) product.setTrackInventory(request.trackInventory());
        if (request.attributes()    != null) product.setAttributes(request.attributes());
        if (request.imageUrl()      != null) product.setImageUrl(request.imageUrl());

        if (request.categoryId() != null) {
            ProductCategory category = categoryRepository
                    .findByIdAndBusinessId(request.categoryId(), businessId)
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Category", request.categoryId()
                            )
                    );
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);

        return ProductDTO.from(
                saved,
                stockService.getCurrentStock(id),
                stockService.getReservedStock(id)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void archiveProduct(UUID id) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Product product = productRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Product", id));

        // Prevent archiving a product that has stock reserved
        int reserved = stockService.getReservedStock(id);
        if (reserved > 0) {
            throw new BusinessRuleException(
                    "PRODUCT_HAS_RESERVATIONS",
                    "Cannot archive product with " + reserved
                            + " units reserved against active orders"
            );
        }

        product.setIsActive(false);
        productRepository.save(product);
        log.info("Product archived: {}", product.getSku());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts() {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        return productRepository
                .findLowStockProducts(businessId)
                .stream()
                .map(product -> ProductDTO.from(
                        product,
                        stockService.getCurrentStock(product.getId()),
                        stockService.getReservedStock(product.getId())
                ))
                .toList();
    }

    // ── Stock operations ──────────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Transactional
    public StockMovementDTO adjustStock(StockAdjustmentRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Product product = productRepository
                .findByIdAndBusinessId(request.productId(), businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Product", request.productId())
                );

        if (!product.getTrackInventory()) {
            throw new BusinessRuleException(
                    "INVENTORY_NOT_TRACKED",
                    "This product does not track inventory"
            );
        }

        var movement = stockService.adjustStock(
                product, request.quantity(), request.reason()
        );

        return StockMovementDTO.from(movement);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Transactional(readOnly = true)
    public Page<StockMovementDTO> getStockMovements(UUID productId,
                                                    Pageable pageable) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        // Verify the product belongs to this business before returning movements
        productRepository.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Product", productId)
                );

        return movementRepository
                .findAllByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(StockMovementDTO::from);
    }

    // ── Categories ────────────────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE', 'ACCOUNTS')")
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategories() {
        UUID businessId = SecurityUtils.getCurrentBusinessId();
        return categoryRepository
                .findAllByBusinessId(businessId)
                .stream()
                .map(CategoryDTO::from)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryDTO createCategory(
            CategoryDTO.CreateCategoryRequest request) {

        UUID businessId = SecurityUtils.getCurrentBusinessId();

        ProductCategory category = ProductCategory.builder()
                .businessId(businessId)
                .name(request.name())
                .build();

        if (request.parentId() != null) {
            ProductCategory parent = categoryRepository
                    .findByIdAndBusinessId(request.parentId(), businessId)
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Parent category", request.parentId()
                            )
                    );
            category.setParent(parent);
        }

        return CategoryDTO.from(categoryRepository.save(category));
    }
}