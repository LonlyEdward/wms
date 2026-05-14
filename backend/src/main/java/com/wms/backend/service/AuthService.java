package com.wms.backend.service;

import com.wms.backend.dto.auth.LoginRequest;
import com.wms.backend.dto.auth.TokenResponse;
import com.wms.backend.dto.auth.UserResponse;
import com.wms.backend.entity.RefreshToken;
import com.wms.backend.entity.User;
import com.wms.backend.exception.AppException;
import com.wms.backend.exception.EntityNotFoundException;
import com.wms.backend.repository.RefreshTokenRepository;
import com.wms.backend.repository.UserRepository;
import com.wms.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.refresh-days}")
    private int refreshDays;

    //Login

    @Transactional
    public TokenResponse login(LoginRequest request, String userAgent,
                               String ipAddress) {
        //Ask Spring Security to verify email + password
        //This calls CustomUserDetailsService.loadUserByUsername()
        //then compares the password using BCrypt
        //If invalid it throws AuthenticationException caught by GlobalExceptionHandler
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS",
                    "Invalid email or password"
            );
        }

        //Load the user from the database
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new EntityNotFoundException("User", request.email())
                );

        //Check the account is active
        if (!user.getIsActive()) {
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    "ACCOUNT_INACTIVE",
                    "Your account has been deactivated"
            );
        }

        //Generate tokens
        String accessToken  = jwtUtil.generateAccessToken(user);
        String rawRefresh   = jwtUtil.generateRefreshToken();
        String hashedRefresh = hashToken(rawRefresh);

        //Save the refresh token hash to the database
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashedRefresh)
                .expiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS))
                .revoked(false)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        //Set businessId on the refresh token
        refreshToken.setBusinessId(user.getBusinessId());

        refreshTokenRepository.save(refreshToken);

        //Update last login timestamp
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());

        return TokenResponse.of(
                accessToken,
                rawRefresh,         //send the raw token to the client
                UserResponse.from(user)
        );
    }

    //Refresh

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        //Hash the incoming token to compare with stored hash
        String hashedToken = hashToken(rawRefreshToken);

        //Find the token in the database
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(hashedToken)
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "INVALID_REFRESH_TOKEN",
                        "Invalid refresh token"
                ));

        //Check it has not been revoked
        if (refreshToken.getRevoked()) {
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    "TOKEN_REVOKED",
                    "Refresh token has been revoked"
            );
        }

        //Check it has not expired
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    "TOKEN_EXPIRED",
                    "Refresh token has expired, please log in again"
            );
        }

        //Generate a new access token
        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user);

        return TokenResponse.of(
                newAccessToken,
                rawRefreshToken,    //return the same refresh token
                UserResponse.from(user)
        );
    }

    //Logout

    @Transactional
    public void logout(String rawRefreshToken) {
        String hashedToken = hashToken(rawRefreshToken);

        refreshTokenRepository.findByTokenHash(hashedToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    //Get current user

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        return UserResponse.from(user);
    }

    //Token hashing
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}