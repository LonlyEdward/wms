package com.wms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Product parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> variants = new ArrayList<>();

    @Column(nullable = false, length = 30)
    private String unitOfMeasure = "UNIT";

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salePrice;

    @Column(nullable = false)
    private Integer reorderPoint = 10;

    @Column(nullable = false)
    private Boolean trackInventory = true;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(columnDefinition = "jsonb")
    private String attributes;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;
}