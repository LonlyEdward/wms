package com.wms.backend.security;

import com.wms.backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    // Private constructor
    private SecurityUtils() {}

    // Get the currently authenticated User entity
    // This works because JwtAuthFilter sets the User as the principal
    public static User getCurrentUser() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException(
                    "No authenticated user found in security context"
            );
        }

        return (User) auth.getPrincipal();
    }

    // Get the current user UUID
    public static UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    // Get the current user businessId
    // used in every service query to scope data to the correct business
    public static UUID getCurrentBusinessId() {
        return getCurrentUser().getBusinessId();
    }

    // Check if the current user has a specific role
    public static boolean hasRole(String role) {
        return getCurrentUser().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}