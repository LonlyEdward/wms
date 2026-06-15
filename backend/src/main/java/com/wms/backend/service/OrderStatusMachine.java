package com.wms.backend.service;

import com.wms.backend.entity.OrderStatus;
import com.wms.backend.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class OrderStatusMachine {

    // The complete map of valid transitions
    // Key: current status
    // Value: set of statuses this order is allowed to move to
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS =
            Map.of(
                    OrderStatus.NEW,        Set.of(
                            OrderStatus.CONFIRMED,
                            OrderStatus.CANCELLED
                    ),
                    OrderStatus.CONFIRMED,  Set.of(
                            OrderStatus.PROCESSING,
                            OrderStatus.CANCELLED
                    ),
                    OrderStatus.PROCESSING, Set.of(
                            OrderStatus.DISPATCHED,
                            // Allow reverting to CONFIRMED
                            // if a fulfilment issue is found
                            OrderStatus.CONFIRMED
                    ),
                    OrderStatus.DISPATCHED, Set.of(
                            OrderStatus.DELIVERED
                    ),
                    OrderStatus.DELIVERED,  Set.of(
                            OrderStatus.INVOICED
                    ),
                    OrderStatus.INVOICED,   Set.of(
                            OrderStatus.CLOSED
                    ),
                    // Terminal statuses
                    // no transitions allowed from here
                    OrderStatus.CLOSED,     Set.of(),
                    OrderStatus.CANCELLED,  Set.of()
            );

    // Check if a transition is valid without throwing
    public boolean isValid(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> allowed = VALID_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    // Validate and throw a clear error if the transition is not allowed
    public void validate(OrderStatus from, OrderStatus to) {
        if (!isValid(from, to)) {
            throw new BusinessRuleException(
                    "INVALID_ORDER_TRANSITION",
                    "Cannot move order from "
                            + from.name()
                            + " to "
                            + to.name()
                            + ". Allowed transitions from "
                            + from.name()
                            + ": "
                            + getAllowedTransitions(from)
            );
        }
    }

    // Returns a readable string of allowed next statuses
    private String getAllowedTransitions(OrderStatus from) {
        Set<OrderStatus> allowed = VALID_TRANSITIONS.get(from);
        if (allowed == null || allowed.isEmpty()) {
            return "none (terminal status)";
        }
        return allowed.toString();
    }
}