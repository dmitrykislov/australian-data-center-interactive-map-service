package com.datacenter.analytics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AnalyticsServiceTest {

  @Autowired
  private AnalyticsService analyticsService;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @BeforeEach
  void setUp() {
    // Clear Redis before each test
    redisTemplate.delete("analytics:page-views");
    redisTemplate.delete("analytics:api-calls");
    redisTemplate.delete("analytics:errors");
  }

  @Test
  void testTrackPageViewWithPath() {
    analyticsService.trackPageView("/");
    Long size = redisTemplate.opsForList().size("analytics:page-views");
    assertTrue(size > 0, "Page view should be tracked");
  }

  @Test
  void testTrackPageViewWithPathAndReferrer() {
    analyticsService.trackPageView("/map", "https://example.com", "desktop");
    Long size = redisTemplate.opsForList().size("analytics:page-views");
    assertTrue(size > 0, "Page view with referrer should be tracked");
  }

  @Test
  void testPageViewDoesNotContainPII() {
    analyticsService.trackPageView("/map");
    String json = redisTemplate.opsForList().index("analytics:page-views", 0);
    assertNotNull(json);
    assertFalse(json.contains("ip"), "Should not contain IP address");
    assertFalse(json.contains("user"), "Should not contain user ID");
    assertFalse(json.contains("session"), "Should not contain session ID");
  }

  @Test
  void testPageViewContainsTimestamp() {
    analyticsService.trackPageView("/");
    String json = redisTemplate.opsForList().index("analytics:page-views", 0);
    assertNotNull(json);
    assertTrue(json.contains("timestamp"), "Should contain timestamp");
  }

  @Test
  void testTrackAPICall() {
    analyticsService.trackApiCall("/api/v1/datacenters", 200);
    Long size = redisTemplate.opsForList().size("analytics:api-calls");
    assertTrue(size > 0, "API call should be tracked");
  }

  @Test
  void testTrackError() {
    analyticsService.trackError("api_error", "Internal server error");
    Long size = redisTemplate.opsForList().size("analytics:errors");
    assertTrue(size > 0, "Error should be tracked");
  }
}