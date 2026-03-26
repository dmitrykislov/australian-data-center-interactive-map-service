package com.datacenter.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DataCenter Tests")
class DataCenterTest {

  private String validId;
  private Coordinates validCoordinates;

  @BeforeEach
  void setUp() {
    validId = UUID.randomUUID().toString();
    validCoordinates = new Coordinates(40.7128, -74.0060);
  }

  @Test
  @DisplayName("should create DataCenter with all fields")
  void testCreateDataCenterWithAllFields() {
    DataCenter dc =
        new DataCenter(
            validId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL,
            "Primary data center",
            "us,east,primary");

    assertEquals(validId, dc.getId());
    assertEquals("NYC Data Center", dc.getName());
    assertEquals("TechCorp", dc.getOperator());
    assertEquals(validCoordinates, dc.getCoordinates());
    assertEquals(500, dc.getCapacity());
    assertEquals(DataCenterStatus.OPERATIONAL, dc.getStatus());
    assertEquals("Primary data center", dc.getDescription());
    assertEquals("us,east,primary", dc.getTags());
  }

  @Test
  @DisplayName("should create DataCenter with required fields only")
  void testCreateDataCenterWithRequiredFieldsOnly() {
    DataCenter dc =
        new DataCenter(
            validId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL);

    assertEquals(validId, dc.getId());
    assertEquals("NYC Data Center", dc.getName());
    assertNull(dc.getDescription());
    assertNull(dc.getTags());
  }

  @Test
  @DisplayName("should reject null id")
  void testRejectNullId() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                null,
                "NYC Data Center",
                "TechCorp",
                validCoordinates,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject empty id")
  void testRejectEmptyId() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                "",
                "NYC Data Center",
                "TechCorp",
                validCoordinates,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject invalid UUID id")
  void testRejectInvalidUuidId() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                "not-a-uuid",
                "NYC Data Center",
                "TechCorp",
                validCoordinates,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should normalize uppercase UUID to lowercase")
  void testNormalizeUppercaseUuid() {
    String uppercaseId = "550E8400-E29B-41D4-A716-446655440000";
    DataCenter dc =
        new DataCenter(
            uppercaseId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL);

    assertEquals(uppercaseId.toLowerCase(), dc.getId());
  }

  @Test
  @DisplayName("should accept mixed case UUID and normalize to lowercase")
  void testAcceptMixedCaseUuid() {
    String mixedCaseId = "550e8400-E29B-41d4-A716-446655440000";
    DataCenter dc =
        new DataCenter(
            mixedCaseId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL);

    assertEquals(mixedCaseId.toLowerCase(), dc.getId());
  }

  @Test
  @DisplayName("should reject null name")
  void testRejectNullName() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                validId,
                null,
                "TechCorp",
                validCoordinates,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject empty name")
  void testRejectEmptyName() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                validId,
                "",
                "TechCorp",
                validCoordinates,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject name exceeding 255 characters")
  void testRejectNameExceeding255Chars() {
    String longName = "a".repeat(256);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                validId,
                longName,
                "TechCorp",
                validCoordinates,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should accept name with exactly 255 characters")
  void testAcceptNameWith255Chars() {
    String name = "a".repeat(255);
    assertDoesNotThrow(
        () ->
            new DataCenter(
                validId,
                name,
                "TechCorp",
                validCoordinates,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject null operator")
  void testRejectNullOperator() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                validId,
                "NYC Data Center",
                null,
                validCoordinates,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject empty operator")
  void testRejectEmptyOperator() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                validId,
                "NYC Data Center",
                "",
                validCoordinates,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject operator exceeding 255 characters")
  void testRejectOperatorExceeding255Chars() {
    String longOperator = "a".repeat(256);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                validId,
                "NYC Data Center",
                longOperator,
                validCoordinates,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject null coordinates")
  void testRejectNullCoordinates() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                validId,
                "NYC Data Center",
                "TechCorp",
                null,
                500,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject zero capacity")
  void testRejectZeroCapacity() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                validId,
                "NYC Data Center",
                "TechCorp",
                validCoordinates,
                0,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject negative capacity")
  void testRejectNegativeCapacity() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                validId,
                "NYC Data Center",
                "TechCorp",
                validCoordinates,
                -100,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should accept positive capacity")
  void testAcceptPositiveCapacity() {
    assertDoesNotThrow(
        () ->
            new DataCenter(
                validId,
                "NYC Data Center",
                "TechCorp",
                validCoordinates,
                1,
                DataCenterStatus.OPERATIONAL));
  }

  @Test
  @DisplayName("should reject null status")
  void testRejectNullStatus() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                validId,
                "NYC Data Center",
                "TechCorp",
                validCoordinates,
                500,
                null));
  }

  @Test
  @DisplayName("should implement equals correctly")
  void testEquals() {
    DataCenter dc1 =
        new DataCenter(
            validId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL);
    DataCenter dc2 =
        new DataCenter(
            validId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL);
    DataCenter dc3 =
        new DataCenter(
            UUID.randomUUID().toString(),
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL);

    assertEquals(dc1, dc2);
    assertNotEquals(dc1, dc3);
    assertNotEquals(dc1, null);
  }

  @Test
  @DisplayName("should implement hashCode correctly")
  void testHashCode() {
    DataCenter dc1 =
        new DataCenter(
            validId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL);
    DataCenter dc2 =
        new DataCenter(
            validId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL);

    assertEquals(dc1.hashCode(), dc2.hashCode());
  }

  @Test
  @DisplayName("should provide meaningful toString")
  void testToString() {
    DataCenter dc =
        new DataCenter(
            validId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL);
    String str = dc.toString();
    assertTrue(str.contains("NYC Data Center"));
    assertTrue(str.contains("TechCorp"));
    assertTrue(str.contains("500"));
  }
}