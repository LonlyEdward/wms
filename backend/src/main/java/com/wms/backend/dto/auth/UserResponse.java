package com.wms.backend.dto.auth;

import com.wms.backend.entity.Role;
import com.wms.backend.entity.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        Role role,
        UUID businessId,
        Boolean notifEmail,
        Boolean notifSms,
        Boolean notifWhatsapp
) {
    // converts a user entity to a UserResponse DTO
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getRole(),
                user.getBusinessId(),
                user.getNotifEmail(),
                user.getNotifSms(),
                user.getNotifWhatsapp()
        );
    }
}