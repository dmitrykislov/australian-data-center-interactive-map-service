package com.datacenter.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SecurityHeadersFilterTest {

  private SecurityHeadersFilter filter;

  @Mock private ServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain chain;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    filter = new SecurityHeadersFilter();
  }

  @Test
  void testHSTSHeaderIsSet() throws IOException, ServletException {
    filter.doFilter(request, response, chain);

    verify(response).setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
  }

  @Test
  void testCSPHeaderIsSet() throws IOException, ServletException {
    filter.doFilter(request, response, chain);

    verify(response).setHeader(eq("Content-Security-Policy"), contains("default-src 'self'"));
  }

  @Test
  void testXFrameOptionsHeaderIsSet() throws IOException, ServletException {
    filter.doFilter(request, response, chain);

    verify(response).setHeader("X-Frame-Options", "DENY");
  }

  @Test
  void testXContentTypeOptionsHeaderIsSet() throws IOException, ServletException {
    filter.doFilter(request, response, chain);

    verify(response).setHeader("X-Content-Type-Options", "nosniff");
  }

  @Test
  void testXXSSProtectionHeaderIsSet() throws IOException, ServletException {
    filter.doFilter(request, response, chain);

    verify(response).setHeader("X-XSS-Protection", "1; mode=block");
  }

  @Test
  void testReferrerPolicyHeaderIsSet() throws IOException, ServletException {
    filter.doFilter(request, response, chain);

    verify(response).setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
  }

  @Test
  void testPermissionsPolicyHeaderIsSet() throws IOException, ServletException {
    filter.doFilter(request, response, chain);

    verify(response).setHeader(eq("Permissions-Policy"), contains("geolocation=()"));
  }

  @Test
  void testFilterChainContinues() throws IOException, ServletException {
    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void testNonHttpResponseIsHandledGracefully() throws IOException, ServletException {
    ServletResponse nonHttpResponse = mock(ServletResponse.class);

    assertDoesNotThrow(() -> filter.doFilter(request, nonHttpResponse, chain));
    verify(chain).doFilter(request, nonHttpResponse);
  }
}