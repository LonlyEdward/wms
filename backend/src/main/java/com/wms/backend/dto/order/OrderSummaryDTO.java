package com.wms.backend.dto.order;

import com.wms.backend.entity.Order;
import com.wms.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// DTO for paginated order lists
// Does not include items or status history
public record OrderSummaryDTO(
        UUID        id,
        String      orderNumber,
        String      customerName,
        UUID        customerId,
        OrderStatus status,
        String      orderType,
        Integer     itemCount,
        BigDecimal  totalAmount,
        Boolean     isFlagged,
        Instant     createdAt,
        Instant     confirmedAt,
        Instant     dispatchedAt
) {
    public static OrderSummaryDTO from(Order order) {
        return new OrderSummaryDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getName(),
                order.getCustomer().getId(),
                order.getStatus(),
                order.getOrderType(),
                order.getItems() != null ? order.getItems().size() : 0,
                order.getTotalAmount(),
                order.getIsFlagged(),
                order.getCreatedAt(),
                order.getConfirmedAt(),
                order.getDispatchedAt()
        );
    }
}