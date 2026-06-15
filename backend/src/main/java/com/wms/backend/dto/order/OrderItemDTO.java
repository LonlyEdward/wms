package com.wms.backend.dto.order;

import com.wms.backend.entity.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDTO(
        UUID       id,
        UUID       productId,
        String     productName,
        String     productSku,
        Integer    quantityOrdered,
        Integer    quantityPicked,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String     notes
) {
    public static OrderItemDTO from(OrderItem item) {
        return new OrderItemDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSku(),
                item.getQuantityOrdered(),
                item.getQuantityPicked(),
                item.getUnitPrice(),
                item.getLineTotal(),
                item.getNotes()
        );
    }
}