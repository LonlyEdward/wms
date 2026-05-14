package com.wms.backend.config;

import com.wms.backend.security.CustomUserDetailsService;
import com.wms.backend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    //Password encoder

    @Bean
    public PasswordEncoder passwordEncoder() {
        //BCrypt with strength 12 each hash takes ~250ms
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
                //Disable CSRF — not needed for stateless REST APIs
                //CSRF is a browser vulnerability. Since we use JWT in headers
                //(not cookies), CSRF attacks cannot occur.
                .csrf(AbstractHttpConfigurer::disable)

                //Configuring CORS to allow the frontend to call this API
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //Configuring which routes are public and which require auth
                .authorizeHttpRequests(auth -> auth

                        //Public routes where no token is needed
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/health",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        //Webhook routes, signature verified inside the handler
                        .requestMatchers(
                                "/api/v1/payments/webhook/**"
                        ).permitAll()

                        //All other routes require authentication
                        .anyRequest().authenticated()
                )

                //Stateless session,  Spring Security will not create HTTP sessions
                //Every request must carry its own JWT, no server side session state
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                //Register our authentication provider
                .authenticationProvider(authenticationProvider())

                //Add our JWT filter BEFORE Spring built in login filter
                //means every request is checked for a JWT first
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    //CORS configuration

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        //Allow requests from React frontend
        config.setAllowedOriginPatterns(
                List.of(frontendUrl, "http://localhost:*")
        );

        //Allow these HTTP methods
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        );

        //Allow all headers including Authorization where the JWT goes
        config.setAllowedHeaders(List.of("*"));

        //Allow credentials (cookies, Authorization header)
        config.setAllowCredentials(true);

        //Cache preflight response for 1 hour
        //Browsers send an OPTIONS preflight before cross origin requests
        //This tells the browser it does not need to preflight every time
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        //Apply this CORS config to all API routes
        source.registerCorsConfiguration("/api/**", config);

        return source;
    }
}