# Security Implementation Guide

## Overview

This document outlines the security measures implemented in the Australian Data Centers Mapping application to protect user data and prevent common web vulnerabilities.

## HTTPS Enforcement

All data transmission is encrypted using HTTPS with TLS 1.2 or higher.

### Configuration
- **Port**: 8443 (HTTPS)
- **Certificate**: Self-signed or CA-signed certificate in PKCS12 format
- **HTTP Redirect**: All HTTP requests are automatically redirected to HTTPS
- **HSTS**: HTTP Strict-Transport-Security header enforces HTTPS for 1 year

### Implementation
- Spring Security `requiresChannel()` enforces HTTPS for all requests
- SecurityHeadersFilter adds HSTS header with `max-age=31536000`
- Browsers cache HTTPS requirement for 1 year

## Security Headers

### HSTS (HTTP Strict-Transport-Security)
- **Value**: `max-age=31536000; includeSubDomains; preload`
- **Purpose**: Forces HTTPS for 1 year, includes subdomains, enables HSTS preload list
- **Protection**: Prevents downgrade attacks and man-in-the-middle attacks

### CSP (Content-Security-Policy)
- **Default**: `default-src 'self'` - only allow resources from same origin
- **Script**: Allows scripts from self and trusted CDNs (cdnjs.cloudflare.com)
- **Style**: Allows styles from self and trusted CDNs
- **Images**: Allows images from self, data URIs, and HTTPS sources
- **Fonts**: Allows fonts from self and trusted CDNs
- **Connect**: Allows connections to self only (prevents data exfiltration)
- **Frame Ancestors**: `frame-ancestors 'none'` - prevents clickjacking
- **Base URI**: `base-uri 'self'` - prevents base tag injection
- **Form Action**: `form-action 'self'` - prevents form hijacking

### X-Frame-Options
- **Value**: `DENY`
- **Purpose**: Prevents clickjacking attacks by disallowing framing

### X-Content-Type-Options
- **Value**: `nosniff`
- **Purpose**: Prevents MIME type sniffing attacks

### X-XSS-Protection
- **Value**: `1; mode=block`
- **Purpose**: Enables XSS filter in legacy browsers

### Referrer-Policy
- **Value**: `strict-origin-when-cross-origin`
- **Purpose**: Controls referrer information sent with requests

### Permissions-Policy
- **Value**: Disables geolocation, microphone, camera, and payment APIs
- **Purpose**: Restricts access to sensitive browser features

## Input Validation

### Validation Rules
- **Path Validation**: Only alphanumeric, slashes, hyphens, underscores allowed
- **UUID Validation**: Case-insensitive UUID format validation
- **Status Code**: Only 100-599 range allowed
- **Search Query**: Maximum 255 characters, alphanumeric and basic punctuation
- **Operator/Region**: Maximum 255 characters, alphanumeric and basic punctuation

### Injection Attack Prevention
- **SQL Injection**: Input patterns prevent SQL keywords and special characters
- **XSS**: Input validation rejects HTML/JavaScript tags
- **Path Traversal**: Rejects `../` and similar patterns
- **Command Injection**: Rejects shell metacharacters
- **LDAP Injection**: Rejects LDAP special characters
- **XML External Entity**: Rejects XML declarations and entity definitions
- **Null Byte Injection**: Rejects null bytes in input

## Analytics Security

### No PII Collection
- **Page Views**: Only timestamp and page path logged
- **API Calls**: Only timestamp, endpoint, and status code logged
- **No IP Addresses**: User IP addresses are never captured
- **No User IDs**: No user identification information collected
- **No Session IDs**: Session identifiers are not stored
- **No Cookies**: Cookie data is not logged

### Data Retention
- **TTL**: Analytics data retained for 30 days in Redis
- **Automatic Cleanup**: Expired data automatically removed by Redis

### Endpoint Security
- **HTTPS Only**: All analytics endpoints require HTTPS
- **Input Validation**: All parameters validated before processing
- **Rate Limiting**: Can be configured to prevent abuse
- **CORS**: Same-origin requests only

## Attribution Security

### Data Source Attribution
- **Tile Providers**: OpenStreetMap and other map tile sources properly attributed
- **Data Sources**: Australian data center information sources credited
- **Links**: Attribution links open in new tabs with `rel="noopener noreferrer"`
- **HTML Escaping**: Attribution text properly escaped to prevent XSS

## CORS Configuration

### Allowed Origins
- `http://localhost:8080` (development)
- `https://localhost:8080` (development with HTTPS)

### Allowed Methods
- GET, POST, PUT, DELETE, OPTIONS

### Allowed Headers
- All headers allowed (can be restricted in production)

### Credentials
- Credentials allowed for same-origin requests

## Best Practices

### Development
1. Use HTTPS even in development
2. Test security headers with browser developer tools
3. Validate all user input
4. Never log sensitive information
5. Keep dependencies updated

### Production
1. Use valid SSL/TLS certificates from trusted CAs
2. Enable HSTS preload list submission
3. Monitor security headers with tools like securityheaders.com
4. Implement rate limiting on analytics endpoints
5. Regular security audits and penetration testing
6. Keep all dependencies patched and updated

## References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP Secure Coding Practices](https://owasp.org/www-community/controls/Secure_Coding_Practices)
- [MDN Web Security](https://developer.mozilla.org/en-US/docs/Web/Security)
- [Spring Security Documentation](https://spring.io/projects/spring-security)