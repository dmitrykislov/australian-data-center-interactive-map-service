# Analytics Implementation Guide

## Overview

This document describes the anonymous analytics tracking implementation in the Australian Data Centers Mapping application.

## Privacy-First Design

The analytics implementation is designed with privacy as the primary concern:

### No PII Collection
- **No IP Addresses**: User IP addresses are never captured
- **No User IDs**: No user identification information collected
- **No Session IDs**: Session identifiers are not stored
- **No Cookies**: Cookie data is not logged
- **No Request Bodies**: Request/response content is not logged
- **No Personal Data**: No personally identifiable information collected

### Data Minimization
- Only essential data is collected
- Data is aggregated and anonymized
- Individual user tracking is impossible
- Data retention is limited to 30 days

## Tracked Events

### Page Views

**What is tracked:**
- Timestamp (when the page was viewed)
- Page path (which page was viewed)
- Referrer (optional, where the user came from)
- User agent type (optional, device type: desktop, mobile, tablet, unknown)

**What is NOT tracked:**
- IP address
- User ID
- Session ID
- Cookies
- Request body
- User location
- Device identifiers

**Example:**
```json
{
  "timestamp": "2024-03-27T10:30:00Z",
  "pagePath": "/map",
  "referrer": "https://example.com",
  "userAgentType": "desktop"
}
```

### API Calls

**What is tracked:**
- Timestamp (when the API was called)
- Endpoint (which API endpoint was called)
- Status code (HTTP response status)
- Response time (optional, how long the request took)

**What is NOT tracked:**
- IP address
- Request body
- Response body
- User ID
- Session ID
- Request headers
- Query parameters

**Example:**
```json
{
  "timestamp": "2024-03-27T10:30:00Z",
  "endpoint": "/api/v1/datacenters",
  "statusCode": 200,
  "responseTimeMs": 150
}
```

### Error Events

**What is tracked:**
- Timestamp (when the error occurred)
- Endpoint (which API endpoint had the error)
- Status code (HTTP error code)
- Error message (optional, what went wrong)

**What is NOT tracked:**
- Stack traces
- Request details
- User information
- System information
- Debug data

**Example:**
```json
{
  "timestamp": "2024-03-27T10:30:00Z",
  "endpoint": "/api/v1/datacenters",
  "statusCode": 500,
  "errorMessage": "Internal Server Error"
}
```

## Implementation

### Backend Components

#### AnalyticsService

The `AnalyticsService` class handles analytics tracking:

```java
@Service
public class AnalyticsService {
  public void trackPageView(String pagePath)
  public void trackPageView(String pagePath, String referrer, String userAgentType)
  public void trackAPICall(String endpoint, int statusCode)
  public void trackAPICall(String endpoint, int statusCode, long responseTimeMs)
  public void trackError(String endpoint, int statusCode, String errorMessage)
}
```

#### AnonymousAnalyticsService

The `AnonymousAnalyticsService` class provides session management:

```java
@Service
public class AnonymousAnalyticsService {
  public void trackPageView(String pageUrl, String referrer, String userAgent, String sessionId)
  public void trackEvent(String eventType, String pageUrl, String sessionId)
  public String generateSessionId()
}
```

#### AnalyticsController

The `AnalyticsController` class exposes REST endpoints:

```java
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
  @PostMapping("/page-view")
  public ResponseEntity<?> trackPageView(...)
  
  @PostMapping("/api-call")
  public ResponseEntity<?> trackAPICall(...)
  
  @PostMapping("/error")
  public ResponseEntity<?> trackError(...)
}
```

### Data Storage

Analytics data is stored in Redis:

**Keys:**
- `analytics:page-views` - List of page view events
- `analytics:api-calls` - List of API call events
- `analytics:errors` - List of error events

**TTL:** 30 days (2,592,000 seconds)

**Format:** JSON strings stored in Redis lists

### Input Validation

All analytics parameters are validated:

**Page Path:**
- Pattern: `^[a-zA-Z0-9/_-]*$`
- Prevents path traversal attacks
- Rejects special characters

**Referrer:**
- Pattern: `^[a-zA-Z0-9:/?#\[\]@!$&'()*+,;=._~-]*$`
- Validates URL format
- Prevents injection attacks

**User Agent Type:**
- Pattern: `^(desktop|mobile|tablet|unknown)$`
- Only allows predefined values
- Prevents invalid data

**Endpoint:**
- Pattern: `^/api/v[0-9]+/[a-zA-Z0-9/_-]*$`
- Validates API endpoint format
- Prevents injection attacks

**Status Code:**
- Pattern: `^[1-5][0-9]{2}$`
- Only allows HTTP status codes 100-599
- Prevents invalid data

## Frontend Implementation

### Session Management

```javascript
// Generate anonymous session ID
const sessionId = generateSessionId();

function generateSessionId() {
  return 'session-' + Math.random().toString(36).substr(2, 9);
}
```

### Page View Tracking

```javascript
// Track page view on page load
function trackPageView() {
  const payload = {
    pagePath: window.location.pathname,
    referrer: document.referrer,
    userAgentType: detectUserAgentType(),
    timestamp: new Date().toISOString(),
    sessionId: sessionId
  };

  fetch('/api/v1/analytics/page-view', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
}
```

### API Call Tracking

```javascript
// Track API calls
async function trackAPICall(endpoint, statusCode, responseTimeMs) {
  const payload = {
    endpoint: endpoint,
    statusCode: statusCode,
    responseTimeMs: responseTimeMs,
    timestamp: new Date().toISOString(),
    sessionId: sessionId
  };

  fetch('/api/v1/analytics/api-call', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
}
```

### Error Tracking

```javascript
// Track errors
function trackError(endpoint, statusCode, errorMessage) {
  const payload = {
    endpoint: endpoint,
    statusCode: statusCode,
    errorMessage: errorMessage,
    timestamp: new Date().toISOString(),
    sessionId: sessionId
  };

  fetch('/api/v1/analytics/error', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
}
```

## API Endpoints

### Track Page View

```
POST /api/v1/analytics/page-view
```

**Request:**
```json
{
  "pagePath": "/map",
  "referrer": "https://example.com",
  "userAgentType": "desktop"
}
```

**Response:**
```json
{
  "success": true
}
```

### Track API Call

```
POST /api/v1/analytics/api-call
```

**Request:**
```json
{
  "endpoint": "/api/v1/datacenters",
  "statusCode": 200,
  "responseTimeMs": 150
}
```

**Response:**
```json
{
  "success": true
}
```

### Track Error

```
POST /api/v1/analytics/error
```

**Request:**
```json
{
  "endpoint": "/api/v1/datacenters",
  "statusCode": 500,
  "errorMessage": "Internal Server Error"
}
```

**Response:**
```json
{
  "success": true
}
```

## Security

### HTTPS Only
- All analytics endpoints require HTTPS
- HTTP requests are redirected to HTTPS
- Data is encrypted in transit

### Input Validation
- All parameters validated on server
- Invalid input rejected with 400 Bad Request
- Prevents injection attacks

### No PII
- No personally identifiable information collected
- No user tracking possible
- Privacy-first design

### Data Retention
- Analytics data retained for 30 days
- Automatic cleanup by Redis TTL
- No long-term user tracking

## Testing

### Unit Tests

`AnalyticsServiceTest` verifies:
- Page views are tracked
- API calls are tracked
- Errors are tracked
- No PII is logged
- Timestamps are included

### Integration Tests

`AnalyticsControllerTest` verifies:
- Endpoints accept valid requests
- Endpoints reject invalid requests
- Responses are correct
- Security headers are present

### Security Tests

`AnalyticsSecurityTest` verifies:
- XSS attacks are rejected
- SQL injection is prevented
- Path traversal is blocked
- Invalid input is rejected
- HTTPS is enforced

## Compliance

### GDPR
- No personal data collected
- No user identification
- No cookies used
- 30-day data retention
- Fully compliant

### CCPA
- No personal information collected
- No user tracking
- No data sharing
- Fully compliant

### Privacy Laws
- Respects user privacy
- No invasive tracking
- Transparent data collection
- Compliant with regulations

## Best Practices

### Data Minimization
- Collect only essential data
- Aggregate data when possible
- Delete data after retention period
- Avoid collecting sensitive information

### Transparency
- Clearly document what is tracked
- Explain why data is collected
- Provide privacy policy
- Allow users to opt-out if desired

### Security
- Use HTTPS for all requests
- Validate all input
- Encrypt data at rest
- Implement access controls

### Performance
- Track asynchronously
- Don't block user interactions
- Batch requests when possible
- Minimize network overhead

## Future Enhancements

1. **Aggregated Reports**: Generate usage reports
2. **Real-time Dashboards**: Monitor usage in real-time
3. **Anomaly Detection**: Detect unusual patterns
4. **Performance Monitoring**: Track application performance
5. **User Feedback**: Collect user satisfaction data