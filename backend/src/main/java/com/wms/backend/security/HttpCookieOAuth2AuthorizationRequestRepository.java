package com.wms.backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    // Cookie name for storing the OAuth2 state
    private static final String COOKIE_NAME = "oauth2_auth_request";
    // Cookie expiry — 3 minutes is plenty for the OAuth2 flow to complete
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(
            HttpServletRequest request) {
        return getCookieValue(request, COOKIE_NAME)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (authorizationRequest == null) {
            deleteCookie(request, response, COOKIE_NAME);
            return;
        }
        // Serialize the request to a Base64 string and store in a cookie
        addCookie(response, COOKIE_NAME,
                serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest =
                loadAuthorizationRequest(request);
        deleteCookie(request, response, COOKIE_NAME);
        return authRequest;
    }

    // ── Cookie helpers ────────────────────────────────────────────────────────

    private java.util.Optional<String> getCookieValue(
            HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return java.util.Optional.of(cookie.getValue());
                }
            }
        }
        return java.util.Optional.empty();
    }

    private void addCookie(HttpServletResponse response,
                           String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);  // not accessible via JavaScript
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletRequest request,
                              HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    private String serialize(OAuth2AuthorizationRequest object) {
        return Base64.getUrlEncoder().encodeToString(
                SerializationUtils.serialize(object)
        );
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(value)
        );
    }
}