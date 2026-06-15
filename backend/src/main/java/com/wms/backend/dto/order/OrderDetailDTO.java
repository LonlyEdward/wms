package com.wms.backend.dto.order;

import com.wms.backend.entity.Order;
import com.wms.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Full order detail including items and status history
// Used on the order detail page
public record OrderDetailDTO(
        UUID                       id,
        String                     orderNumber,
        UUID                       customerId,
        String                     customerName,
        String                     customerEmail,
        OrderStatus                status,
        String                     orderType,
        String                     notes,
        Boolean                    isFlagged,
        String                     flagReason,
        BigDecimal                 subtotal,
        BigDecimal                 taxAmount,
        BigDecimal                 totalAmount,
        String                     deliveryAddressSnapshot,
        List<OrderItemDTO>         items,
        List<OrderStatusHistoryDTO> statusHistory,
        String                     placedByName,
        Instant                    createdAt,
        Instant                    confirmedAt,
        Instant                    dispatchedAt,
        Instant                    deliveredAt
) {
    public static OrderDetailDTO from(Order order) {
        return new OrderDetailDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getCustomer().getEmail(),
                order.getStatus(),
                order.getOrderType(),
                order.getNotes(),
                order.getIsFlagged(),
                order.getFlagReason(),
                order.getSubtotal(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                order.getDeliveryAddressSnapshot(),
                order.getItems().stream()
                        .map(OrderItemDTO::from)
                        .toList(),
                order.getStatusHistory().stream()
                        .map(OrderStatusHistoryDTO::from)
                        .toList(),
                order.getPlacedBy() != null
                        ? order.getPlacedBy().getFullName()
                        : "Portal",
                order.getCreatedAt(),
                order.getConfirmedAt(),
                order.getDispatchedAt(),
                order.getDeliveredAt()
        );
    }
}