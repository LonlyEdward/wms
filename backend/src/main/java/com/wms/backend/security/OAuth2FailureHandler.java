package com.wms.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException {

        log.warn("OAuth2 login failed: {}", exception.getMessage());

        // Encode the error message so it is safe to include in a URL
        String errorMessage = URLEncoder.encode(
                exception.getMessage(),
                StandardCharsets.UTF_8
        );

        // Redirect to the frontend login page with an error message
        response.sendRedirect(
                frontendUrl + "/login?error=" + errorMessage
        );
    }
}