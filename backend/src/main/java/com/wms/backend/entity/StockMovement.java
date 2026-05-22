package com.wms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "stock_movements")
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer quantityBefore;

    @Column(nullable = false)
    private Integer quantityAfter;

    @Column(length = 30)
    private String referenceType;

    private UUID referenceId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;
}