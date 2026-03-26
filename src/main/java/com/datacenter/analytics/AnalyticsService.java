package com.datacenter.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for anonymous analytics tracking.
 * Logs page views and API calls without capturing personally identifiable information (PII).
 * No user IDs, IP addresses, or session identifiers are stored.
 */
@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    private static final String PAGE_VIEWS_KEY = "analytics:page-views";
    private static final String API_CALLS_KEY = "analytics:api-calls";
    private static final String ERRORS_KEY = "analytics:errors";
    private static final long ANALYTICS_TTL_DAYS = 30;

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public AnalyticsService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Tracks an anonymous page view (overloaded - single parameter).
     * Logs: timestamp, page path.
     * Does NOT log: IP address, user ID, session ID, cookies, or any PII.
     *
     * @param pagePath the page path (e.g., "/", "/map", "/search")
     */
    public void trackPageView(String pagePath) {
        trackPageView(pagePath, null, null);
    }

    /**
     * Tracks an anonymous page view.
     * Logs: timestamp, page path, referrer (optional), user agent type (optional).
     * Does NOT log: IP address, user ID, session ID, cookies, or any PII.
     *
     * @param pagePath the page path (e.g., "/", "/map", "/search")
     * @param referrer the HTTP referrer (optional, may be null)
     * @param userAgentType the user agent type (desktop, mobile, tablet, unknown)
     */
    public void trackPageView(String pagePath, String referrer, String userAgentType) {
        try {
            Map<String, String> event = new HashMap<>();
            event.put("timestamp", Instant.now().toString());
            event.put("pagePath", pagePath);
            if (referrer != null && !referrer.isEmpty()) {
                event.put("referrer", referrer);
            }
            if (userAgentType != null && !userAgentType.isEmpty()) {
                event.put("userAgentType", userAgentType);
            }

            String eventJson = serializeEvent(event);
            ListOperations<String, String> listOps = redisTemplate.opsForList();
            listOps.rightPush(PAGE_VIEWS_KEY, eventJson);
            redisTemplate.expire(PAGE_VIEWS_KEY, Duration.ofDays(ANALYTICS_TTL_DAYS));

            logger.debug("Tracked page view: path={}, referrer={}, userAgentType={}",
                pagePath, referrer, userAgentType);
        } catch (Exception e) {
            logger.warn("Analytics unavailable - could not track page view: {}", e.getMessage());
        }
    }

    /**
     * Tracks an anonymous API call (overloaded - two parameters).
     * Logs: timestamp, endpoint, status code.
     * Does NOT log: request body, response body, IP address, user ID, or any PII.
     *
     * @param endpoint the API endpoint (e.g., "/api/datacenters", "/api/search")
     * @param statusCode the HTTP response status code
     */
    public void trackApiCall(String endpoint, int statusCode) {
        trackApiCall(endpoint, statusCode, null);
    }

    /**
     * Tracks an anonymous API call.
     * Logs: timestamp, endpoint, status code, response time (optional).
     * Does NOT log: request body, response body, IP address, user ID, or any PII.
     *
     * @param endpoint the API endpoint (e.g., "/api/datacenters", "/api/search")
     * @param statusCode the HTTP response status code
     * @param responseTimeMs the response time in milliseconds (optional, may be null)
     */
    public void trackApiCall(String endpoint, int statusCode, Long responseTimeMs) {
        try {
            Map<String, String> event = new HashMap<>();
            event.put("timestamp", Instant.now().toString());
            event.put("endpoint", endpoint);
            event.put("statusCode", String.valueOf(statusCode));
            if (responseTimeMs != null) {
                event.put("responseTimeMs", String.valueOf(responseTimeMs));
            }

            String eventJson = serializeEvent(event);
            ListOperations<String, String> listOps = redisTemplate.opsForList();
            listOps.rightPush(API_CALLS_KEY, eventJson);
            redisTemplate.expire(API_CALLS_KEY, Duration.ofDays(ANALYTICS_TTL_DAYS));

            logger.debug("Tracked API call: endpoint={}, statusCode={}, responseTimeMs={}",
                endpoint, statusCode, responseTimeMs);
        } catch (Exception e) {
            logger.warn("Analytics unavailable - could not track API call: {}", e.getMessage());
        }
    }

    /**
     * Tracks an error event.
     * Logs: timestamp, error type, error message.
     * Does NOT log: stack traces, user data, or any PII.
     *
     * @param errorType the type of error (e.g., "validation_error", "network_error")
     * @param errorMessage the error message
     */
    public void trackError(String errorType, String errorMessage) {
        try {
            Map<String, String> event = new HashMap<>();
            event.put("timestamp", Instant.now().toString());
            event.put("errorType", errorType);
            event.put("errorMessage", errorMessage);

            String eventJson = serializeEvent(event);
            ListOperations<String, String> listOps = redisTemplate.opsForList();
            listOps.rightPush(ERRORS_KEY, eventJson);
            redisTemplate.expire(ERRORS_KEY, Duration.ofDays(ANALYTICS_TTL_DAYS));

            logger.debug("Tracked error: type={}, message={}", errorType, errorMessage);
        } catch (Exception e) {
            logger.warn("Analytics unavailable - could not track error event: {}", e.getMessage());
        }
    }

    /**
     * Serializes an event map to JSON string.
     * Simple implementation without external JSON library dependency.
     *
     * @param event the event map
     * @return JSON string representation
     */
    private String serializeEvent(Map<String, String> event) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : event.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(entry.getKey())).append("\":");
            json.append("\"").append(escapeJson(entry.getValue())).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    /**
     * Escapes special characters in JSON strings.
     *
     * @param value the value to escape
     * @return escaped value
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}