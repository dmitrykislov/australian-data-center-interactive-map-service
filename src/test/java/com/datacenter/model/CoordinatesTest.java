package com.datacenter.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Coordinates Tests")
class CoordinatesTest {

  @Test
  @DisplayName("should create valid coordinates")
  void testCreateValidCoordinates() {
    Coordinates coords = new Coordinates(40.7128, -74.0060);
    assertEquals(40.7128, coords.getLatitude());
    assertEquals(-74.0060, coords.getLongitude());
  }

  @Test
  @DisplayName("should accept boundary latitude values")
  void testBoundaryLatitudes() {
    assertDoesNotThrow(() -> new Coordinates(-90.0, 0.0));
    assertDoesNotThrow(() -> new Coordinates(90.0, 0.0));
    assertDoesNotThrow(() -> new Coordinates(0.0, 0.0));
  }

  @Test
  @DisplayName("should accept boundary longitude values")
  void testBoundaryLongitudes() {
    assertDoesNotThrow(() -> new Coordinates(0.0, -180.0));
    assertDoesNotThrow(() -> new Coordinates(0.0, 180.0));
    assertDoesNotThrow(() -> new Coordinates(0.0, 0.0));
  }

  @ParameterizedTest
  @ValueSource(doubles = {-90.1, -91.0, 90.1, 91.0})
  @DisplayName("should reject invalid latitude values")
  void testInvalidLatitudes(double latitude) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Coordinates(latitude, 0.0),
        "Should reject latitude: " + latitude);
  }

  @ParameterizedTest
  @ValueSource(doubles = {-180.1, -181.0, 180.1, 181.0})
  @DisplayName("should reject invalid longitude values")
  void testInvalidLongitudes(double longitude) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Coordinates(0.0, longitude),
        "Should reject longitude: " + longitude);
  }

  @Test
  @DisplayName("should implement equals correctly")
  void testEquals() {
    Coordinates coords1 = new Coordinates(40.7128, -74.0060);
    Coordinates coords2 = new Coordinates(40.7128, -74.0060);
    Coordinates coords3 = new Coordinates(51.5074, -0.1278);

    assertEquals(coords1, coords2);
    assertNotEquals(coords1, coords3);
    assertNotEquals(coords1, null);
    assertNotEquals(coords1, "not a coordinate");
  }

  @Test
  @DisplayName("should implement hashCode correctly")
  void testHashCode() {
    Coordinates coords1 = new Coordinates(40.7128, -74.0060);
    Coordinates coords2 = new Coordinates(40.7128, -74.0060);

    assertEquals(coords1.hashCode(), coords2.hashCode());
  }

  @Test
  @DisplayName("should provide meaningful toString")
  void testToString() {
    Coordinates coords = new Coordinates(40.7128, -74.0060);
    String str = coords.toString();
    assertTrue(str.contains("40.712800"));
    assertTrue(str.contains("-74.006000"));
  }
}