package com.wms.backend.dto.product;

import com.wms.backend.entity.MovementType;
import com.wms.backend.entity.StockMovement;

import java.time.Instant;
import java.util.UUID;

public record StockMovementDTO(
        UUID id,
        MovementType movementType,
        Integer quantity,
        Integer quantityBefore,
        Integer quantityAfter,
        String referenceType,
        UUID referenceId,
        String reason,
        String performedBy,
        Instant createdAt
) {
    public static StockMovementDTO from(StockMovement movement) {
        return new StockMovementDTO(
                movement.getId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getQuantityBefore(),
                movement.getQuantityAfter(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getReason(),
                movement.getPerformedBy() != null
                        ? movement.getPerformedBy().getFullName()
                        : "System",
                movement.getCreatedAt()
        );
    }
}