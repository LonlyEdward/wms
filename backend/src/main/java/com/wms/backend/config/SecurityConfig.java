package com.wms.backend.config;

import com.wms.backend.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter            jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService  oAuth2UserService;
    private final OAuth2SuccessHandler     oAuth2SuccessHandler;
    private final OAuth2FailureHandler     oAuth2FailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository
            authorizationRequestRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // Password encoder

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    //Authentication provider

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    //Authentication manager

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    //Security filter chain

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        // Public routes where no token required
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/health",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/login/oauth2/**",
                                "/oauth2/**"
                        ).permitAll()

                        // Payment webhooks for when I add payments
                        .requestMatchers(
                                "/api/v1/payments/webhook/**"
                        ).permitAll()

                        // Everything else requires a valid JWT
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider())

                // JWT filter runs before Spring's auth filter
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // OAuth2 login configuration
                .oauth2Login(oauth2 -> oauth2

                        // user continues with Google
                        .authorizationEndpoint(endpoint ->
                                endpoint.baseUri("/api/v1/oauth2/authorize")
                                // Use cookie based storage instead of session
                                .authorizationRequestRepository(authorizationRequestRepository)
                        )

                        // The callback URL
                        // Spring exchanges the code for user info here
                        .redirectionEndpoint(endpoint ->
                                endpoint.baseUri("/login/oauth2/code/*")
                        )

                        // Our custom service that saves the user to the database
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(oAuth2UserService)
                        )

                        // Call on success to generate JWT and redirect to frontend
                        .successHandler(oAuth2SuccessHandler)

                        // Call on failure to redirect to frontend with error
                        .failureHandler(oAuth2FailureHandler)
                );

        return http.build();
    }

    // CORS

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(
                List.of(frontendUrl, "http://localhost:*")
        );
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        );
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/login/**", config);
        source.registerCorsConfiguration("/oauth2/**", config);
        return source;
    }
}