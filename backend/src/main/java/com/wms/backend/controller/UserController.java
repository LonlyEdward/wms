package com.wms.backend.controller;

import com.wms.backend.dto.ApiResponse;
import com.wms.backend.dto.auth.UserResponse;
import com.wms.backend.entity.User;
import com.wms.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "Staff user management")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all staff users for this business")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @AuthenticationPrincipal User currentUser) {

        List<UserResponse> users =
                userService.getAllUsers(currentUser.getBusinessId());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new staff user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal User currentUser) {

        UserResponse user = userService.createUser(
                request, currentUser.getBusinessId()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a staff user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User currentUser) {

        UserResponse user = userService.updateUser(
                id, request, currentUser.getBusinessId()
        );
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a staff user")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        userService.deactivateUser(id, currentUser.getBusinessId());
        return ResponseEntity.ok(
                ApiResponse.success(null, "User deactivated")
        );
    }

    public record CreateUserRequest(
            @jakarta.validation.constraints.NotBlank String firstName,
            @jakarta.validation.constraints.NotBlank String lastName,
            @jakarta.validation.constraints.Email String email,
            @jakarta.validation.constraints.NotBlank String password,
            @jakarta.validation.constraints.NotNull com.wms.backend.entity.Role role
    ) {}

    public record UpdateUserRequest(
            String firstName,
            String lastName,
            com.wms.backend.entity.Role role
    ) {}
}