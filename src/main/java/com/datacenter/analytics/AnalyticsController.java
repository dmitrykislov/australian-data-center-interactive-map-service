package com.datacenter.analytics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.datacenter.validation.InputValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for analytics endpoints.
 * Provides endpoints to track page views and API calls anonymously.
 * No PII is captured or stored.
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private InputValidator inputValidator;

    /**
     * Endpoint to track a page view.
     * Called by frontend JavaScript to log page navigation.
     *
     * @param pagePath the page path (e.g., "/", "/map")
     * @param referrer the HTTP referrer (optional)
     * @param userAgentType the user agent type (desktop, mobile, tablet)
     * @return 200 OK with confirmation
     */
    @PostMapping("/page-view")
    public ResponseEntity<Map<String, String>> trackPageView(
            @RequestParam(value = "pagePath", required = false) String pagePath,
            @RequestParam(value = "referrer", required = false) String referrer,
            @RequestParam(value = "userAgentType", required = false, defaultValue = "unknown") String userAgentType) {

        // Validate pagePath parameter (reject empty strings, apply default if null)
        if (pagePath != null && pagePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Page path cannot be empty");
        }
        if (pagePath == null) {
            pagePath = "/";
        }
        inputValidator.validateAnalyticsPath(pagePath);
        
        // Validate referrer if provided
        if (referrer != null && !referrer.trim().isEmpty()) {
            inputValidator.validateAnalyticsReferrer(referrer);
        }
        
        // Validate userAgentType if provided
        if (userAgentType != null && !userAgentType.trim().isEmpty()) {
            inputValidator.validateAnalyticsUserAgentType(userAgentType);
        }

        analyticsService.trackPageView(pagePath, referrer, userAgentType);

        Map<String, String> response = new HashMap<>();
        response.put("status", "tracked");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to track an API call.
     * Called by frontend JavaScript to log API usage patterns.
     *
     * @param endpoint the API endpoint (e.g., "/api/datacenters")
     * @param statusCode the HTTP status code
     * @param responseTimeMs the response time in milliseconds (optional)
     * @return 200 OK with confirmation
     */
    @PostMapping("/api-call")
    public ResponseEntity<Map<String, String>> trackApiCall(
            @RequestParam(value = "endpoint", required = true) String endpoint,
            @RequestParam(value = "statusCode", required = true) int statusCode,
            @RequestParam(value = "responseTimeMs", required = false) Long responseTimeMs) {

        // Validate endpoint parameter
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Endpoint cannot be empty");
        }
        inputValidator.validateAnalyticsEndpoint(endpoint);
        
        // Validate status code
        inputValidator.validateStatusCode(statusCode);

        analyticsService.trackApiCall(endpoint, statusCode, responseTimeMs);

        Map<String, String> response = new HashMap<>();
        response.put("status", "tracked");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to track an error event.
     * Called by frontend JavaScript to log client-side errors.
     *
     * @param errorType the type of error (e.g., "validation_error")
     * @param errorMessage the error message
     * @return 200 OK with confirmation
     */
    @PostMapping("/error")
    public ResponseEntity<Map<String, String>> trackError(
            @RequestParam(value = "errorType") String errorType,
            @RequestParam(value = "errorMessage") String errorMessage) {

        // Validate error type and message
        inputValidator.validateSearchQuery(errorType);
        inputValidator.validateSearchQuery(errorMessage);

        analyticsService.trackError(errorType, errorMessage);

        Map<String, String> response = new HashMap<>();
        response.put("status", "tracked");
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for analytics service.
     *
     * @return 200 OK with status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        return ResponseEntity.ok(response);
    }
}