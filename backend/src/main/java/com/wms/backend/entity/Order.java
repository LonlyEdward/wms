package com.wms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    private CustomerAddress deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.NEW;

    @Column(nullable = false, length = 20)
    private String orderType = "STANDARD";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placed_by")
    private User placedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Boolean isFlagged = false;

    @Column(columnDefinition = "TEXT")
    private String flagReason;

    private Instant confirmedAt;
    private Instant dispatchedAt;
    private Instant deliveredAt;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "jsonb")
    private String deliveryAddressSnapshot;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @OrderBy("changedAt ASC")
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();
}