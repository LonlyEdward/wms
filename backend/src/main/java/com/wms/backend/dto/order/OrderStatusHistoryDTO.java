package com.wms.backend.dto.order;

import com.wms.backend.entity.OrderStatus;
import com.wms.backend.entity.OrderStatusHistory;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusHistoryDTO(
        UUID        id,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        String      changedByName,
        String      reason,
        Instant     changedAt
) {
    public static OrderStatusHistoryDTO from(OrderStatusHistory history) {
        return new OrderStatusHistoryDTO(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedBy() != null
                        ? history.getChangedBy().getFullName()
                        : "System",
                history.getReason(),
                history.getChangedAt()
        );
    }
}