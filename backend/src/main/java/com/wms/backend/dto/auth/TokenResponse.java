package com.wms.backend.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserResponse user
) {
    // constructor that always sets tokenType to bearer
    public static TokenResponse of(String accessToken,
                                   String refreshToken,
                                   UserResponse user) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", user);
    }
}