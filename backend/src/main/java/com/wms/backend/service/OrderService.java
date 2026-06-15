package com.wms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.backend.dto.order.*;
import com.wms.backend.entity.*;
import com.wms.backend.exception.BusinessRuleException;
import com.wms.backend.exception.EntityNotFoundException;
import com.wms.backend.repository.*;
import com.wms.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository             orderRepository;
    private final OrderItemRepository         orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ProductRepository           productRepository;
    private final CustomerRepository          customerRepository;
    private final CustomerAddressRepository   addressRepository;
    private final StockService                stockService;
    private final CustomerService             customerService;
    private final OrderStatusMachine          statusMachine;
    private final AuditService                auditService;
    private final ObjectMapper                objectMapper;

    //Create order

    @PreAuthorize("hasAnyRole('ADMIN', 'BUYER')")
    @Transactional
    public OrderDetailDTO createOrder(CreateOrderRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();
        User currentUser = SecurityUtils.getCurrentUser();

        // Validate the customer

        Customer customer = customerRepository
                .findByIdAndBusinessId(request.customerId(), businessId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Customer", request.customerId())
                );

        // Build order items and validate stock

        List<OrderItemData> itemDataList = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {

            Product product = productRepository
                    .findByIdAndBusinessId(itemReq.productId(), businessId)
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Product", itemReq.productId()
                            )
                    );

            if (!product.getIsActive()) {
                throw new BusinessRuleException(
                        "PRODUCT_INACTIVE",
                        "Product '" + product.getName()
                                + "' is no longer available"
                );
            }

            // Check available stock before reserving
            if (product.getTrackInventory()) {
                int available =
                        stockService.getAvailableStock(product.getId());
                if (available < itemReq.quantity()) {
                    throw new BusinessRuleException(
                            "INSUFFICIENT_STOCK",
                            "Only " + available
                                    + " units of '"
                                    + product.getName()
                                    + "' available. "
                                    + itemReq.quantity()
                                    + " requested."
                    );
                }
            }

            // Resolve the unit price
            // Use override price if provided, otherwise use product default
            BigDecimal unitPrice = itemReq.unitPriceOverride() != null
                    ? itemReq.unitPriceOverride()
                    : product.getSalePrice();

            BigDecimal lineTotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemReq.quantity())
            );

            subtotal = subtotal.add(lineTotal);

            itemDataList.add(new OrderItemData(
                    product, itemReq.quantity(), unitPrice, lineTotal,
                    itemReq.notes()
            ));
        }

        // Check credit limit
        // Calculate tax on the subtotal
        // Tax rate comes from the business configuration
        // For now use 18% as the standard rate
        BigDecimal taxRate   = BigDecimal.valueOf(18);
        BigDecimal taxAmount = subtotal
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxAmount);

        // This throws AccountOnHoldException or CreditLimitExceededException
        // if the order cannot proceed
        // caught by GlobalExceptionHandler
        customerService.checkCreditLimit(customer.getId(), total);

        // Resolve delivery address

        CustomerAddress deliveryAddress = null;
        if (request.deliveryAddressId() != null) {
            deliveryAddress = addressRepository
                    .findByIdAndCustomerId(
                            request.deliveryAddressId(),
                            customer.getId()
                    )
                    .orElse(null);
        } else {
            // Use the customer's default address if no specific one given
            deliveryAddress = customer.getAddresses().stream()
                    .filter(CustomerAddress::getIsDefault)
                    .findFirst()
                    .orElse(null);
        }

        // Snapshot the address at order time so it is preserved even if
        // the customer later changes or deletes their address
        String addressSnapshot = null;
        if (deliveryAddress != null) {
            addressSnapshot = toJson(deliveryAddress);
        }

        // Create the order

        Order order = Order.builder()
                .businessId(businessId)
                .orderNumber(orderRepository.generateOrderNumber())
                .customer(customer)
                .deliveryAddress(deliveryAddress)
                .deliveryAddressSnapshot(addressSnapshot)
                .status(OrderStatus.NEW)
                .orderType(request.orderType())
                .notes(request.notes())
                .isFlagged(false)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .totalAmount(total)
                .placedBy(currentUser)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Create order items

        for (OrderItemData data : itemDataList) {
            // The productSnapshot captures the product state at order time
            // Therefore historical orders always show the original price
            // even if the product price changes later
            String productSnapshot = toJson(java.util.Map.of(
                    "id",   data.product().getId().toString(),
                    "name", data.product().getName(),
                    "sku",  data.product().getSku(),
                    "price", data.unitPrice().toString()
            ));

            OrderItem item = OrderItem.builder()
                    .businessId(businessId)
                    .order(savedOrder)
                    .product(data.product())
                    .productSnapshot(productSnapshot)
                    .quantityOrdered(data.quantity())
                    .quantityPicked(0)
                    .unitPrice(data.unitPrice())
                    .lineTotal(data.lineTotal())
                    .notes(data.notes())
                    .build();

            orderItemRepository.save(item);
            savedOrder.getItems().add(item);
        }

        //Record initial status history

        recordStatusHistory(
                savedOrder, null, OrderStatus.NEW, currentUser,
                "Order created"
        );

        // Audit log

        auditService.log(
                "CREATE", "Order",
                savedOrder.getId(),
                null,
                java.util.Map.of(
                        "orderNumber", savedOrder.getOrderNumber(),
                        "customer",    customer.getName(),
                        "total",       total.toString()
                )
        );

        log.info("Order created: {} for customer: {}",
                savedOrder.getOrderNumber(), customer.getName());

        return OrderDetailDTO.from(savedOrder);
    }

    //Get orders

    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTS', 'WAREHOUSE')")
    @Transactional(readOnly = true)
    public Page<OrderSummaryDTO> getOrders(OrderStatus status,
                                           UUID customerId,
                                           Instant from,
                                           Instant to,
                                           String search,
                                           Pageable pageable) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        return orderRepository
                .searchOrders(businessId, status, customerId,
                        from, to, search, pageable)
                .map(OrderSummaryDTO::from);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTS', 'WAREHOUSE')")
    @Transactional(readOnly = true)
    public OrderDetailDTO getOrderById(UUID id) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Order order = orderRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Order", id));

        return OrderDetailDTO.from(order);
    }

    // Status transitions

    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Transactional
    public OrderDetailDTO updateStatus(UUID id,
                                       UpdateOrderStatusRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();
        User currentUser = SecurityUtils.getCurrentUser();

        Order order = orderRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Order", id));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.newStatus();

        // Validate the transition is allowed
        statusMachine.validate(oldStatus, newStatus);

        // Require a reason for cancellations and reversals
        if ((newStatus == OrderStatus.CANCELLED
                || newStatus == OrderStatus.CONFIRMED
                && oldStatus == OrderStatus.PROCESSING)
                && (request.reason() == null
                || request.reason().isBlank())) {
            throw new BusinessRuleException(
                    "REASON_REQUIRED",
                    "A reason is required for this status change"
            );
        }

        switch (newStatus) {
            case CONFIRMED -> {
                // When confirmed: reserve stock for all items
                for (OrderItem item : order.getItems()) {
                    if (item.getProduct().getTrackInventory()) {
                        stockService.reserveStock(
                                item.getProduct(),
                                item.getQuantityOrdered(),
                                order.getId()
                        );
                    }
                }
                order.setConfirmedAt(Instant.now());
            }

            case CANCELLED -> {
                // When cancelled: release all stock reservations
                // Only release if the order was previously CONFIRMED or PROCESSING
                // reservations only exist from CONFIRMED onwards
                if (oldStatus == OrderStatus.CONFIRMED
                        || oldStatus == OrderStatus.PROCESSING) {
                    for (OrderItem item : order.getItems()) {
                        if (item.getProduct().getTrackInventory()) {
                            stockService.releaseReservation(
                                    item.getProduct(),
                                    item.getQuantityOrdered(),
                                    order.getId()
                            );
                        }
                    }
                }
            }

            case DISPATCHED -> {
                // When dispatched: deduct actual stock from warehouse
                // StockService handles releasing the reservation
                // and recording the physical OUT movement
                for (OrderItem item : order.getItems()) {
                    if (item.getProduct().getTrackInventory()) {
                        stockService.deductStock(
                                item.getProduct(),
                                item.getQuantityOrdered(),
                                order.getId()
                        );
                    }
                }
                order.setDispatchedAt(Instant.now());
            }

            case DELIVERED -> order.setDeliveredAt(Instant.now());

            default -> {}
        }

        // Update the order status
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        // Record the transition in status history
        recordStatusHistory(saved, oldStatus, newStatus,
                currentUser, request.reason());

        // Audit log
        auditService.logStatusChange(
                "Order", saved.getId(),
                oldStatus.name(), newStatus.name()
        );

        log.info("Order {} status: {} → {}",
                order.getOrderNumber(), oldStatus, newStatus);

        return OrderDetailDTO.from(saved);
    }

    //  shortcuts

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderDetailDTO confirmOrder(UUID id) {
        return updateStatus(
                id,
                new UpdateOrderStatusRequest(OrderStatus.CONFIRMED, "Confirmed by admin")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderDetailDTO cancelOrder(UUID id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException(
                    "REASON_REQUIRED",
                    "A reason is required to cancel an order"
            );
        }
        return updateStatus(
                id,
                new UpdateOrderStatusRequest(OrderStatus.CANCELLED, reason)
        );
    }

    // Flag management

    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Transactional
    public OrderDetailDTO flagOrder(UUID id, FlagOrderRequest request) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Order order = orderRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Order", id));

        order.setIsFlagged(true);
        order.setFlagReason(request.reason());
        Order saved = orderRepository.save(order);

        auditService.log(
                "UPDATE", "Order", saved.getId(),
                java.util.Map.of("flagged", false),
                java.util.Map.of("flagged", true, "reason", request.reason())
        );

        log.warn("Order {} flagged: {}", order.getOrderNumber(),
                request.reason());

        return OrderDetailDTO.from(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderDetailDTO unflagOrder(UUID id) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();

        Order order = orderRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new EntityNotFoundException("Order", id));

        order.setIsFlagged(false);
        order.setFlagReason(null);
        Order saved = orderRepository.save(order);

        return OrderDetailDTO.from(saved);
    }

    //Buyer portal orders

    @PreAuthorize("hasRole('BUYER')")
    @Transactional(readOnly = true)
    public Page<OrderSummaryDTO> getMyOrders(Pageable pageable) {
        UUID businessId = SecurityUtils.getCurrentBusinessId();
        User currentUser = SecurityUtils.getCurrentUser();

        // Find the customer account linked to this buyer user
        Customer customer = customerRepository
                .findByUserIdAndBusinessId(currentUser.getId(), businessId)
                .orElseThrow(() -> new BusinessRuleException(
                        "NO_CUSTOMER_ACCOUNT",
                        "No customer account found for this user. "
                                + "Please contact the admin."
                ));

        return orderRepository
                .findAllByCustomerIdAndBusinessId(
                        customer.getId(), businessId, pageable
                )
                .map(OrderSummaryDTO::from);
    }

    //Private helpers

    private void recordStatusHistory(Order order,
                                     OrderStatus fromStatus,
                                     OrderStatus toStatus,
                                     User changedBy,
                                     String reason) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedBy(changedBy);
        history.setReason(reason);
        history.setChangedAt(Instant.now());

        historyRepository.save(history);
        order.getStatusHistory().add(history);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Could not serialise to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }

    // Private record to hold order item data during order creation
    // avoids multiple variables in the loop
    private record OrderItemData(
            Product    product,
            Integer    quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            String     notes
    ) {}
}