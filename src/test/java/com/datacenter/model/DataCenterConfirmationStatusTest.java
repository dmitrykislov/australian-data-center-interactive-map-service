package com.datacenter.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DataCenter Confirmation Status Tests")
class DataCenterConfirmationStatusTest {

  private static final String VALID_UUID = "550e8400-e29b-41d4-a716-446655440001";
  private static final Coordinates VALID_COORDS = new Coordinates(-33.8688, 151.2093);

  @Test
  @DisplayName("should create data center with confirmed status")
  void testCreateDataCenterWithConfirmedStatus() {
    DataCenter dc =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            "Test description",
            "test,tags",
            "confirmed",
            null);

    assertEquals("confirmed", dc.getConfirmationStatus());
  }

  @Test
  @DisplayName("should create data center with unconfirmed status")
  void testCreateDataCenterWithUnconfirmedStatus() {
    DataCenter dc =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            "Test description",
            "test,tags",
            "unconfirmed",
            null);

    assertEquals("unconfirmed", dc.getConfirmationStatus());
  }

  @Test
  @DisplayName("should normalize confirmation status to lowercase")
  void testNormalizeConfirmationStatusToLowercase() {
    DataCenter dc =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            "Test description",
            "test,tags",
            "CONFIRMED",
            null);

    assertEquals("confirmed", dc.getConfirmationStatus());
  }

  @Test
  @DisplayName("should normalize mixed case confirmation status")
  void testNormalizeMixedCaseConfirmationStatus() {
    DataCenter dc =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            "Test description",
            "test,tags",
            "UnConfirmed",
            null);

    assertEquals("unconfirmed", dc.getConfirmationStatus());
  }

  @Test
  @DisplayName("should default to confirmed when confirmation status is null")
  void testDefaultToConfirmedWhenNull() {
    DataCenter dc =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            "Test description",
            "test,tags",
            null,
            null);

    assertEquals("confirmed", dc.getConfirmationStatus());
  }

  @Test
  @DisplayName("should reject invalid confirmation status")
  void testRejectInvalidConfirmationStatus() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                VALID_UUID,
                "Test DC",
                "TestOp",
                VALID_COORDS,
                100,
                DataCenterStatus.OPERATIONAL,
                "Test description",
                "test,tags",
                "invalid",
                null));
  }

  @Test
  @DisplayName("should reject empty confirmation status")
  void testRejectEmptyConfirmationStatus() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new DataCenter(
                VALID_UUID,
                "Test DC",
                "TestOp",
                VALID_COORDS,
                100,
                DataCenterStatus.OPERATIONAL,
                "Test description",
                "test,tags",
                "",
                null));
  }

  @Test
  @DisplayName("should maintain confirmation status in equality check")
  void testConfirmationStatusInEquality() {
    DataCenter dc1 =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            "Test description",
            "test,tags",
            "confirmed",
            null);

    DataCenter dc2 =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            "Test description",
            "test,tags",
            "confirmed",
            null);

    assertEquals(dc1, dc2);
  }

  @Test
  @DisplayName("should differ when confirmation status differs")
  void testDifferenceInConfirmationStatus() {
    DataCenter dc1 =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            "Test description",
            "test,tags",
            "confirmed",
            null);

    DataCenter dc2 =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            "Test description",
            "test,tags",
            "unconfirmed",
            null);

    assertNotEquals(dc1, dc2);
  }

  @Test
  @DisplayName("should include confirmation status in string representation")
  void testConfirmationStatusInToString() {
    DataCenter dc =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            "Test description",
            "test,tags",
            "confirmed",
            null);

    String str = dc.toString();
    assertTrue(str.contains("confirmed"));
  }
}