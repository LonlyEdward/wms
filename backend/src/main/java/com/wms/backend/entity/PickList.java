package com.wms.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pick_lists")
public class PickList extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PickListStatus status = PickListStatus.PENDING;

    private Instant startedAt;
    private Instant completedAt;

    @Column(columnDefinition = "TEXT")
    private String dispatchNotePath;

    @Column(columnDefinition = "TEXT")
    private String podImagePath;

    private Instant podCapturedAt;

    private UUID driverId;

    @Column(columnDefinition = "TEXT")
    private String notes;
}