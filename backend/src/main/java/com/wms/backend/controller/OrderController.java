package com.wms.backend.controller;

import com.wms.backend.dto.ApiResponse;
import com.wms.backend.dto.order.*;
import com.wms.backend.entity.OrderStatus;
import com.wms.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Order management and lifecycle")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "List orders with optional filters")
    public ResponseEntity<ApiResponse<Page<OrderSummaryDTO>>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrders(status, customerId, from, to,
                        search, pageable)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full order detail")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> getOrder(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                ApiResponse.success(orderService.getOrderById(id))
        );
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        orderService.createOrder(request),
                        "Order created successfully"
                ));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.updateStatus(id, request)
        ));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a pending order")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> confirmOrder(
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.confirmOrder(id),
                "Order confirmed"
        ));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> cancelOrder(
            @PathVariable UUID id,
            @RequestParam String reason) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.cancelOrder(id, reason),
                "Order cancelled"
        ));
    }

    @PostMapping("/{id}/flag")
    @Operation(summary = "Flag an order for attention")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> flagOrder(
            @PathVariable UUID id,
            @Valid @RequestBody FlagOrderRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.flagOrder(id, request),
                "Order flagged"
        ));
    }

    @PostMapping("/{id}/unflag")
    @Operation(summary = "Remove flag from an order")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> unflagOrder(
            @PathVariable UUID id) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.unflagOrder(id),
                "Order unflagged"
        ));
    }

    // Buyer portal endpoint
    // returns only the current buyer's orders
    @GetMapping("/my")
    @Operation(summary = "Get orders for the current buyer")
    public ResponseEntity<ApiResponse<Page<OrderSummaryDTO>>> getMyOrders(
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                orderService.getMyOrders(pageable)
        ));
    }
}