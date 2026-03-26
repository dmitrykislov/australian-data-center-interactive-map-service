package com.datacenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for HTTPS enforcement and security headers.
 * Implements defense-in-depth with HSTS, CSP, X-Frame-Options, and HTTPS redirect.
 * 
 * Note: CSP and other security headers are set by SecurityHeadersFilter (@Component)
 * to ensure consistent application across all responses.
 */
@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfig {

    /**
     * Configures HTTP security with HTTPS enforcement.
     * - Redirects HTTP to HTTPS (only in production)
     * - Sets HSTS header (max-age: 31536000 seconds = 1 year)
     * - Sets X-Frame-Options to prevent clickjacking
     * - Sets X-XSS-Protection for legacy browser support
     * - Configures CORS for same-origin requests
     * 
     * CSP is handled by SecurityHeadersFilter for consistency.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .requiresChannel(channel -> channel.anyRequest().requiresSecure())
            .headers(headers -> headers
                .xssProtection(xss -> {})
                .frameOptions(frame -> frame.deny())
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }

    /**
     * CORS configuration allowing same-origin requests only.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "https://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}