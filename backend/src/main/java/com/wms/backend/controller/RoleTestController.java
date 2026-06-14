package com.wms.backend.controller;

import com.wms.backend.dto.ApiResponse;
import com.wms.backend.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@Tag(name = "Role Tests", description = "Temporary endpoints to verify role enforcement")
public class RoleTestController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin only endpoint")
    public ResponseEntity<ApiResponse<String>> adminOnly(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                "Hello " + user.getFirstName() + ", you are ADMIN"
        ));
    }

    @GetMapping("/warehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Admin or Warehouse endpoint")
    public ResponseEntity<ApiResponse<String>> warehouseOrAdmin(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                "Hello " + user.getFirstName() + ", role: " + user.getRole()
        ));
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTS')")
    @Operation(summary = "Admin or Accounts endpoint")
    public ResponseEntity<ApiResponse<String>> accountsOrAdmin(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                "Hello " + user.getFirstName() + ", role: " + user.getRole()
        ));
    }

    @GetMapping("/buyer")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Buyer only endpoint")
    public ResponseEntity<ApiResponse<String>> buyerOnly(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                "Hello " + user.getFirstName() + ", you are a BUYER"
        ));
    }

    @GetMapping("/any")
    @Operation(summary = "Any authenticated user")
    public ResponseEntity<ApiResponse<String>> anyAuthenticated(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                "Hello " + user.getFirstName() + ", you are authenticated"
        ));
    }
}