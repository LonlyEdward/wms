package com.wms.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        //Extracting the token from the Authorization header
        String token = extractToken(request);

        //If a token exists and is valid then authenticate the user
        if (token != null && jwtUtil.isTokenValid(token)) {

            //Get the user email from the token
            String email = jwtUtil.extractEmail(token);

            //Only proceed if the SecurityContext is not already set
            //this prevents processing the token multiple times for one request
            if (email != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                //Load the full user from the database
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                //Create a Spring Security authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,               //credentials null because JWT??
                                userDetails.getAuthorities()
                        );

                //Attach request details to the auth object
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                //Put the authentication into the SecurityContext
                //From this point Spring Security knows who this user is
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);

                log.debug("Authenticated user: {}", email);
            }
        }

        //Always continue to the next filter regardless
        filterChain.doFilter(request, response);
    }

    // Extracts Bearer token from the Authorization header
    // Returns just the token string or null if not present
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7); // remove "Bearer " prefix
        }
        return null;
    }
}