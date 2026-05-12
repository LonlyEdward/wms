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
@Table(name = "businesses")
public class Business extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String taxNumber;

    @Column(nullable = false, length = 3)
    private String currencyCode = "TZS";

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.valueOf(18.00);

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, length = 30)
    private String plan = "STANDARD";

    @Column(columnDefinition = "jsonb")
    private String modulesEnabled;

    @OneToMany(mappedBy = "business", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();
}