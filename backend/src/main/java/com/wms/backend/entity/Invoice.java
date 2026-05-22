package com.wms.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceType invoiceType = InvoiceType.TAX;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountOutstanding;

    @Column(columnDefinition = "TEXT")
    private String pdfPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voided_by")
    private User voidedBy;

    @Column(columnDefinition = "TEXT")
    private String voidReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(
            mappedBy = "invoice",
            fetch = FetchType.LAZY
    )
    @OrderBy("createdAt ASC")
    private List<Payment> payments = new ArrayList<>();
}