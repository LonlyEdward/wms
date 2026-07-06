package com.wms.backend.controller;

import com.wms.backend.dto.ApiResponse;
import com.wms.backend.dto.auth.LoginRequest;
import com.wms.backend.dto.auth.RefreshRequest;
import com.wms.backend.dto.auth.TokenResponse;
import com.wms.backend.dto.auth.UserResponse;
import com.wms.backend.entity.User;
import com.wms.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, logout, and token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        TokenResponse response = authService.login(
                request,
                httpRequest.getHeader("User-Agent"),
                httpRequest.getRemoteAddr()
        );

        return ResponseEntity.ok(ApiResponse.success(response,
                "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh an access token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) {

        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshRequest request) {

        authService.logout(request.refreshToken());
        return ResponseEntity.ok(
                ApiResponse.success(null, "Logged out successfully")
        );
    }

    @GetMapping("/oauth2/google")
    @Operation(summary = "Initiate Google OAuth2 login")
    public void initiateGoogleLogin(HttpServletResponse response)
            throws IOException {
        // Redirect to Spring Security's OAuth2 authorization endpoint
        response.sendRedirect("/api/v1/oauth2/authorize/google");
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(
                ApiResponse.success(UserResponse.from(user))
        );
    }
}