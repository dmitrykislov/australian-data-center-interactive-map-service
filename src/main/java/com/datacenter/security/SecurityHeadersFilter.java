package com.datacenter.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Security headers filter that adds OWASP-recommended security headers to all responses.
 * Prevents common web vulnerabilities including XSS, clickjacking, and MIME sniffing.
 */
@Component
public class SecurityHeadersFilter implements Filter {

  private static final String HSTS_HEADER = "Strict-Transport-Security";
  private static final String HSTS_VALUE = "max-age=31536000; includeSubDomains; preload";

  private static final String CSP_HEADER = "Content-Security-Policy";
  private static final String CSP_VALUE =
      "default-src 'self'; "
          + "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://unpkg.com https://cdnjs.cloudflare.com; "
          + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://unpkg.com https://cdnjs.cloudflare.com; "
          + "img-src 'self' data: https:; "
          + "font-src 'self' https://cdn.jsdelivr.net https://unpkg.com https://cdnjs.cloudflare.com; "
          + "connect-src 'self' ws: wss: https://tile.openstreetmap.org https://tiles.stadiamaps.com; "
          + "frame-ancestors 'none'; "
          + "base-uri 'self'; "
          + "form-action 'self'";

  private static final String X_FRAME_OPTIONS = "X-Frame-Options";
  private static final String X_FRAME_OPTIONS_VALUE = "DENY";

  private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
  private static final String X_CONTENT_TYPE_OPTIONS_VALUE = "nosniff";

  private static final String X_XSS_PROTECTION = "X-XSS-Protection";
  private static final String X_XSS_PROTECTION_VALUE = "1; mode=block";

  private static final String REFERRER_POLICY = "Referrer-Policy";
  private static final String REFERRER_POLICY_VALUE = "strict-origin-when-cross-origin";

  private static final String PERMISSIONS_POLICY = "Permissions-Policy";
  private static final String PERMISSIONS_POLICY_VALUE =
      "geolocation=(), microphone=(), camera=(), payment=()";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (response instanceof HttpServletResponse httpResponse) {
      // HSTS: Force HTTPS for 1 year
      httpResponse.setHeader(HSTS_HEADER, HSTS_VALUE);

      // CSP: Prevent XSS and injection attacks
      httpResponse.setHeader(CSP_HEADER, CSP_VALUE);

      // Clickjacking protection
      httpResponse.setHeader(X_FRAME_OPTIONS, X_FRAME_OPTIONS_VALUE);

      // MIME type sniffing prevention
      httpResponse.setHeader(X_CONTENT_TYPE_OPTIONS, X_CONTENT_TYPE_OPTIONS_VALUE);

      // XSS protection (legacy, for older browsers)
      httpResponse.setHeader(X_XSS_PROTECTION, X_XSS_PROTECTION_VALUE);

      // Referrer policy
      httpResponse.setHeader(REFERRER_POLICY, REFERRER_POLICY_VALUE);

      // Permissions policy
      httpResponse.setHeader(PERMISSIONS_POLICY, PERMISSIONS_POLICY_VALUE);
    }

    chain.doFilter(request, response);
  }
}