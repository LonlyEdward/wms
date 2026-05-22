package com.wms.backend.security;

import com.wms.backend.entity.RefreshToken;
import com.wms.backend.entity.User;
import com.wms.backend.repository.RefreshTokenRepository;
import com.wms.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.jwt.refresh-days}")
    private int refreshDays;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        // Step 1: Get the OAuth2User from the authentication object
        // This is what Spring Security gives us after OAuth2 login
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // Step 2: Load the actual User entity from our database
        // We need our User entity because JwtUtil needs it to build the token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found after OAuth2 authentication: " + email
                ));

        // Step 3: Generate a JWT access token for this user
        String accessToken  = jwtUtil.generateAccessToken(user);

        // Step 4: Generate and store a refresh token
        String rawRefresh   = jwtUtil.generateRefreshToken();
        String hashedRefresh = hashToken(rawRefresh);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashedRefresh)
                .expiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS))
                .revoked(false)
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(request.getRemoteAddr())
                .build();

        refreshToken.setBusinessId(user.getBusinessId());
        refreshTokenRepository.save(refreshToken);

        log.info("OAuth2 login successful for: {}", email);

        // Step 5: Build the redirect URL
        // We pass both tokens to the frontend as URL query parameters
        // The frontend reads these, stores them, and removes them from the URL
        String redirectUrl = frontendUrl
                + "/portal/auth/callback"
                + "?token=" + accessToken
                + "&refresh=" + rawRefresh;

        // Step 6: Redirect the browser to the frontend
        response.sendRedirect(redirectUrl);
    }

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