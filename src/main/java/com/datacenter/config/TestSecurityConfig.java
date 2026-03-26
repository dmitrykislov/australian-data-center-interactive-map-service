package com.datacenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Simplified security configuration for local test profile.
 * Allows frontend static assets and API routes to load without auth prompts.
 */
@Configuration
@Profile("test")
public class TestSecurityConfig {

  @Bean
  public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable());

    return http.build();
  }
}

