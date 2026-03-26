package com.datacenter.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Tests for security configuration in test profile.
 * Verifies that TestSecurityConfig is properly loaded.
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

  @Autowired 
  private TestSecurityConfig testSecurityConfig;

  @Autowired
  private SecurityFilterChain securityFilterChain;

  @Test
  void testSecurityConfigBeanExists() {
    assertNotNull(testSecurityConfig, "TestSecurityConfig should be loaded in test profile");
  }

  @Test
  void testSecurityFilterChainBeanExists() {
    assertNotNull(securityFilterChain, "SecurityFilterChain should be available");
  }

  @Test
  void testTestProfileIsActive() {
    // Verify that we're running in test profile
    // TestSecurityConfig should be active, not SecurityConfig
    assertNotNull(testSecurityConfig);
  }
}