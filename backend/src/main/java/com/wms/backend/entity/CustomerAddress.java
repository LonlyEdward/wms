package com.wms.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customer_addresses")
public class CustomerAddress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(length = 50)
    private String label;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String region;

    @Column(nullable = false, length = 100)
    private String country = "Tanzania";

    @Column(nullable = false)
    private Boolean isDefault = false;
}