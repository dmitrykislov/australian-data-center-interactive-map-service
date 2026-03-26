package com.datacenter.analytics;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for tracking anonymous analytics events.
 * Logs page views and user interactions without collecting personally identifiable information.
 */
@Service
public class AnonymousAnalyticsService {

  private static final Logger logger = LoggerFactory.getLogger(AnonymousAnalyticsService.class);

  /**
   * Tracks a page view event.
   *
   * @param pageUrl the URL of the page being viewed
   * @param referrer the referrer URL (may be null)
   * @param userAgent the user agent string (may be null)
   * @param sessionId the anonymous session ID
   */
  public void trackPageView(String pageUrl, String referrer, String userAgent, String sessionId) {
    Objects.requireNonNull(pageUrl, "pageUrl cannot be null");
    Objects.requireNonNull(sessionId, "sessionId cannot be null");

    Map<String, String> properties = new HashMap<>();
    properties.put("pageUrl", pageUrl);
    if (referrer != null) {
      properties.put("referrer", referrer);
    }
    if (userAgent != null) {
      properties.put("userAgent", userAgent);
    }
    properties.put("sessionId", sessionId);

    AnalyticsEvent event = new AnalyticsEvent("page_view", Instant.now(), properties);
    logEvent(event);
  }

  /**
   * Tracks a custom event.
   *
   * @param eventType the type of event
   * @param pageUrl the URL where the event occurred
   * @param sessionId the anonymous session ID
   */
  public void trackEvent(String eventType, String pageUrl, String sessionId) {
    Objects.requireNonNull(eventType, "eventType cannot be null");
    Objects.requireNonNull(pageUrl, "pageUrl cannot be null");
    Objects.requireNonNull(sessionId, "sessionId cannot be null");

    Map<String, String> properties = new HashMap<>();
    properties.put("pageUrl", pageUrl);
    properties.put("sessionId", sessionId);

    AnalyticsEvent event = new AnalyticsEvent(eventType, Instant.now(), properties);
    logEvent(event);
  }

  /**
   * Generates a new anonymous session ID.
   *
   * @return a UUID-based session ID
   */
  public String generateSessionId() {
    return UUID.randomUUID().toString();
  }

  private void logEvent(AnalyticsEvent event) {
    logger.debug(
        "Analytics event: type={}, timestamp={}, properties={}",
        event.getEventType(),
        event.getTimestamp(),
        event.getProperties());
  }
}