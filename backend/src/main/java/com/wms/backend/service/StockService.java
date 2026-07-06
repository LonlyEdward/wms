package com.wms.backend.service;

import com.wms.backend.entity.MovementType;
import com.wms.backend.entity.Product;
import com.wms.backend.entity.StockMovement;
import com.wms.backend.entity.User;
import com.wms.backend.exception.BusinessRuleException;
import com.wms.backend.repository.StockMovementRepository;
import com.wms.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMovementRepository stockMovementRepository;

    // Total stock including reservations
    // what is physically in the warehouse
    @Transactional(readOnly = true)
    public int getCurrentStock(UUID productId) {
        return stockMovementRepository.computeCurrentStock(productId);
    }

    // Stock reserved against confirmed orders
    // stock exists physically but is promised to a customer
    @Transactional(readOnly = true)
    public int getReservedStock(UUID productId) {
        return stockMovementRepository.computeReservedStock(productId);
    }

    // Stock available for new orders
    // Available = current - reserved
    @Transactional(readOnly = true)
    public int getAvailableStock(UUID productId) {
        int current  = getCurrentStock(productId);
        int reserved = getReservedStock(productId);
        return current - reserved;
    }

    // Manual adjustment, called by admin or warehouse staff
    // quantity positive = adding stock (received delivery)
    // quantity negative = removing stock (damage or loss)
    @Transactional
    public StockMovement adjustStock(Product product,
                                     int quantity,
                                     String reason) {

        int before = getCurrentStock(product.getId());
        int after  = before + quantity;

        // Prevent stock going below zero
        if (after < 0) {
            throw new BusinessRuleException(
                    "INSUFFICIENT_STOCK",
                    "Cannot remove " + Math.abs(quantity)
                            + " units. Current stock is " + before + "."
            );
        }

        User currentUser = SecurityUtils.getCurrentUser();

        StockMovement movement = StockMovement.builder()
                .businessId(product.getBusinessId())
                .product(product)
                .movementType(quantity > 0
                        ? MovementType.IN
                        : MovementType.ADJUSTMENT)
                .quantity(quantity)
                .quantityBefore(before)
                .quantityAfter(after)
                .referenceType("MANUAL")
                .reason(reason)
                .performedBy(currentUser)
                .build();

        stockMovementRepository.save(movement);

        log.info("Stock adjusted for product {}: {} → {} ({})",
                product.getSku(), before, after, quantity);

        return movement;
    }

    // Reserve stock when an order is confirmed
    // Creates a negative RESERVATION movement
    // stock is still physically present but allocated to an order
    @Transactional
    public void reserveStock(Product product,
                             int quantity,
                             UUID orderId) {

        int available = getAvailableStock(product.getId());

        if (available < quantity) {
            throw new BusinessRuleException(
                    "INSUFFICIENT_STOCK",
                    "Cannot reserve " + quantity
                            + " units of " + product.getName()
                            + ". Only " + available + " available."
            );
        }

        int before = getCurrentStock(product.getId());

        // Reservation is recorded as a negative quantity
        // It does not change the physical stock, only the available stock
        StockMovement movement = StockMovement.builder()
                .businessId(product.getBusinessId())
                .product(product)
                .movementType(MovementType.RESERVATION)
                .quantity(-quantity)             // negative, reducing available
                .quantityBefore(before)
                .quantityAfter(before)           // physical stock unchanged
                .referenceType("ORDER")
                .referenceId(orderId)
                .reason("Reserved for order")
                .performedBy(SecurityUtils.getCurrentUser())
                .build();

        stockMovementRepository.save(movement);
    }

    // Release a reservation when an order is cancelled
    // Creates a positive RELEASE movement to reverse the reservation
    @Transactional
    public void releaseReservation(Product product,
                                   int quantity,
                                   UUID orderId) {

        StockMovement movement = StockMovement.builder()
                .businessId(product.getBusinessId())
                .product(product)
                .movementType(MovementType.RELEASE)
                .quantity(quantity)              // positive — restoring available
                .quantityBefore(getCurrentStock(product.getId()))
                .quantityAfter(getCurrentStock(product.getId()))
                .referenceType("ORDER")
                .referenceId(orderId)
                .reason("Reservation released — order cancelled")
                .performedBy(SecurityUtils.getCurrentUser())
                .build();

        stockMovementRepository.save(movement);
    }

    // Deduct stock when an order is dispatched
    // This is the actual stock leaving the warehouse
    // Also releases the reservation created when the order was confirmed
    @Transactional
    public void deductStock(Product product,
                            int quantity,
                            UUID orderId) {

        int before = getCurrentStock(product.getId());
        int after  = before - quantity;

        if (after < 0) {
            throw new BusinessRuleException(
                    "INSUFFICIENT_STOCK",
                    "Cannot deduct " + quantity
                            + " units of " + product.getName()
                            + ". Current stock is " + before + "."
            );
        }

        //Release the reservation
        StockMovement release = StockMovement.builder()
                .businessId(product.getBusinessId())
                .product(product)
                .movementType(MovementType.RELEASE)
                .quantity(quantity)
                .quantityBefore(before)
                .quantityAfter(before)
                .referenceType("ORDER")
                .referenceId(orderId)
                .reason("Reservation released on dispatch")
                .performedBy(SecurityUtils.getCurrentUser())
                .build();

        stockMovementRepository.save(release);

        // Record the physical OUT movement
        StockMovement out = StockMovement.builder()
                .businessId(product.getBusinessId())
                .product(product)
                .movementType(MovementType.OUT)
                .quantity(-quantity)
                .quantityBefore(before)
                .quantityAfter(after)
                .referenceType("ORDER")
                .referenceId(orderId)
                .reason("Dispatched to customer")
                .performedBy(SecurityUtils.getCurrentUser())
                .build();

        stockMovementRepository.save(out);

        log.info("Stock deducted for product {}: {} → {}",
                product.getSku(), before, after);
    }
}