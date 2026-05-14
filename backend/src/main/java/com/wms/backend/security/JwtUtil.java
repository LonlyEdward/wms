package com.wms.backend.security;

import com.wms.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiry-minutes}")
    private int expiryMinutes;

    //Token generation

    public String generateAccessToken(User user) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + (long) expiryMinutes * 60 * 1000);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email",      user.getEmail())
                .claim("role",       user.getRole().name())
                .claim("businessId", user.getBusinessId().toString())
                .claim("firstName",  user.getFirstName())
                .claim("lastName",   user.getLastName())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    //Token reading

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractBusinessId(String token) {
        return extractAllClaims(token).get("businessId", String.class);
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    //Token validation

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            // Check the token has not expired
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid signature, malformed token, expired are all caught here
            return false;
        }
    }

    //Signing key

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}