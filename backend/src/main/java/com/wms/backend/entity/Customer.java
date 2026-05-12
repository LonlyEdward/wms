package com.wms.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 30)
    private String accountType = "RETAIL";

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(precision = 15, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(length = 20)
    private String paymentTerms = "NET_30";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(
            mappedBy = "customer",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @OrderBy("isDefault DESC")
    private List<CustomerAddress> addresses = new ArrayList<>();
}