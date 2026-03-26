package com.datacenter.analytics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnonymousAnalyticsServiceTest {

  private AnonymousAnalyticsService analyticsService;

  @BeforeEach
  void setUp() {
    analyticsService = new AnonymousAnalyticsService();
  }

  @Test
  void testGenerateSessionId() {
    String sessionId = analyticsService.generateSessionId();
    assertNotNull(sessionId);
    assertFalse(sessionId.isEmpty());
  }

  @Test
  void testGenerateSessionIdIsUnique() {
    String sessionId1 = analyticsService.generateSessionId();
    String sessionId2 = analyticsService.generateSessionId();
    assertNotEquals(sessionId1, sessionId2);
  }

  @Test
  void testTrackPageView() {
    String sessionId = analyticsService.generateSessionId();
    assertDoesNotThrow(() -> analyticsService.trackPageView("https://example.com", "https://referrer.com", "Mozilla/5.0", sessionId));
  }

  @Test
  void testTrackPageViewWithNullReferrer() {
    String sessionId = analyticsService.generateSessionId();
    assertDoesNotThrow(() -> analyticsService.trackPageView("https://example.com", null, "Mozilla/5.0", sessionId));
  }

  @Test
  void testTrackPageViewWithNullUserAgent() {
    String sessionId = analyticsService.generateSessionId();
    assertDoesNotThrow(() -> analyticsService.trackPageView("https://example.com", "https://referrer.com", null, sessionId));
  }

  @Test
  void testTrackEvent() {
    String sessionId = analyticsService.generateSessionId();
    assertDoesNotThrow(() -> analyticsService.trackEvent("page_view", "https://example.com", sessionId));
  }

  @Test
  void testTrackEventWithDifferentTypes() {
    String sessionId = analyticsService.generateSessionId();
    assertDoesNotThrow(() -> analyticsService.trackEvent("click", "https://example.com", sessionId));
    assertDoesNotThrow(() -> analyticsService.trackEvent("search", "https://example.com", sessionId));
    assertDoesNotThrow(() -> analyticsService.trackEvent("filter", "https://example.com", sessionId));
  }
}