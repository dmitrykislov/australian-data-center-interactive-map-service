package com.datacenter.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * HTTPS enforcement configuration.
 * Ensures all traffic is redirected to HTTPS and security headers are applied.
 */
@Configuration
public class HttpsEnforcer implements WebMvcConfigurer {
  // HTTPS enforcement is handled by SecurityConfig and SecurityHeadersFilter
  // This class serves as a marker for HTTPS enforcement configuration
}