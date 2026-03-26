package com.datacenter.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InputValidatorSecurityTest {

  private InputValidator validator;

  @BeforeEach
  void setUp() {
    validator = new InputValidator();
  }

  @Test
  void testSQLInjectionInSearchQuery() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validateSearchQuery("'; DROP TABLE datacenters; --"));
  }

  @Test
  void testXSSInSearchQuery() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validateSearchQuery("<script>alert('xss')</script>"));
  }

  @Test
  void testPathTraversalInAnalyticsPath() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validateAnalyticsPath("../../../etc/passwd"));
  }

  @Test
  void testNullByteInjectionInSearchQuery() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validateSearchQuery("test\0injection"));
  }

  @Test
  void testCommandInjectionInOperator() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validateOperator("; rm -rf /"));
  }

  @Test
  void testLDAPInjectionInRegion() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validateRegion("*)(uid=*"));
  }

  @Test
  void testXMLExternalEntityInSearchQuery() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validateSearchQuery("<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"));
  }

  @Test
  void testUnicodeBypassAttempt() {
    assertThrows(
        IllegalArgumentException.class,
        () -> validator.validateSearchQuery("\u003cscript\u003e"));
  }

  @Test
  void testValidSearchQueryWithSpecialChars() {
    assertDoesNotThrow(() -> validator.validateSearchQuery("Data-Center_123"));
  }

  @Test
  void testValidOperatorWithHyphen() {
    assertDoesNotThrow(() -> validator.validateOperator("Next-DC"));
  }
}