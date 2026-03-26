package com.datacenter.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InputValidatorTest {

  private InputValidator validator;

  @BeforeEach
  void setUp() {
    validator = new InputValidator();
  }

  @Test
  void testValidateAnalyticsPathValid() {
    assertDoesNotThrow(() -> validator.validateAnalyticsPath("/map"));
    assertDoesNotThrow(() -> validator.validateAnalyticsPath("/api/search"));
  }

  @Test
  void testValidateAnalyticsPathNull() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateAnalyticsPath(null));
  }

  @Test
  void testValidateAnalyticsPathEmpty() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateAnalyticsPath(""));
  }

  @Test
  void testValidateAnalyticsPathTooLong() {
    String longPath = "a".repeat(256);
    assertThrows(IllegalArgumentException.class, () -> validator.validateAnalyticsPath(longPath));
  }

  @Test
  void testValidateAnalyticsPathInvalidCharacters() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateAnalyticsPath("/map<script>"));
    assertThrows(IllegalArgumentException.class, () -> validator.validateAnalyticsPath("/map'; DROP TABLE"));
  }

  @Test
  void testValidateAnalyticsReferrerValid() {
    assertDoesNotThrow(() -> validator.validateAnalyticsReferrer("https://example.com"));
    assertDoesNotThrow(() -> validator.validateAnalyticsReferrer(null));
  }

  @Test
  void testValidateAnalyticsReferrerTooLong() {
    String longReferrer = "https://example.com/" + "a".repeat(2048);
    assertThrows(IllegalArgumentException.class, () -> validator.validateAnalyticsReferrer(longReferrer));
  }

  @Test
  void testValidateAnalyticsUserAgentTypeValid() {
    assertDoesNotThrow(() -> validator.validateAnalyticsUserAgentType("desktop"));
    assertDoesNotThrow(() -> validator.validateAnalyticsUserAgentType("mobile"));
    assertDoesNotThrow(() -> validator.validateAnalyticsUserAgentType("tablet"));
    assertDoesNotThrow(() -> validator.validateAnalyticsUserAgentType("unknown"));
  }

  @Test
  void testValidateAnalyticsUserAgentTypeInvalid() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateAnalyticsUserAgentType("invalid"));
  }

  @Test
  void testValidateAnalyticsEndpointValid() {
    assertDoesNotThrow(() -> validator.validateAnalyticsEndpoint("/api/v1/datacenters"));
    assertDoesNotThrow(() -> validator.validateAnalyticsEndpoint("/api/v1/search"));
  }

  @Test
  void testValidateAnalyticsEndpointInvalid() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateAnalyticsEndpoint("/invalid"));
    assertThrows(IllegalArgumentException.class, () -> validator.validateAnalyticsEndpoint("api/v1/datacenters"));
  }

  @Test
  void testValidateStatusCodeValid() {
    assertDoesNotThrow(() -> validator.validateStatusCode(200));
    assertDoesNotThrow(() -> validator.validateStatusCode(404));
    assertDoesNotThrow(() -> validator.validateStatusCode(500));
  }

  @Test
  void testValidateStatusCodeInvalid() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateStatusCode(99));
    assertThrows(IllegalArgumentException.class, () -> validator.validateStatusCode(600));
  }

  @Test
  void testValidateUUIDValid() {
    assertDoesNotThrow(() -> validator.validateUUID("550e8400-e29b-41d4-a716-446655440000"));
    assertDoesNotThrow(() -> validator.validateUUID("550E8400-E29B-41D4-A716-446655440000"));
  }

  @Test
  void testValidateUUIDInvalid() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateUUID("not-a-uuid"));
    assertThrows(IllegalArgumentException.class, () -> validator.validateUUID("550e8400-e29b-41d4-a716"));
  }

  @Test
  void testValidateSearchQueryValid() {
    assertDoesNotThrow(() -> validator.validateSearchQuery("Sydney"));
    assertDoesNotThrow(() -> validator.validateSearchQuery("AWS-1"));
  }

  @Test
  void testValidateSearchQueryTooLong() {
    String longQuery = "a".repeat(256);
    assertThrows(IllegalArgumentException.class, () -> validator.validateSearchQuery(longQuery));
  }

  @Test
  void testValidateOperatorValid() {
    assertDoesNotThrow(() -> validator.validateOperator("AWS"));
    assertDoesNotThrow(() -> validator.validateOperator("Google Cloud"));
  }

  @Test
  void testValidateRegionValid() {
    assertDoesNotThrow(() -> validator.validateRegion("NSW"));
    assertDoesNotThrow(() -> validator.validateRegion("New South Wales"));
  }

  @Test
  void testSQLInjectionPrevention() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateSearchQuery("'; DROP TABLE datacenters; --"));
  }

  @Test
  void testXSSPrevention() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateSearchQuery("<script>alert('xss')</script>"));
  }

  @Test
  void testPathTraversalPrevention() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateAnalyticsPath("/../../../etc/passwd"));
  }
}