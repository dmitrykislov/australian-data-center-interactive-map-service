package com.datacenter.model;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DataCenter Serialization Tests")
class DataCenterSerializationTest {

  private ObjectMapper objectMapper;
  private String validId;
  private Coordinates validCoordinates;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    validId = UUID.randomUUID().toString();
    validCoordinates = new Coordinates(40.7128, -74.0060);
  }

  @Test
  @DisplayName("should serialize DataCenter to JSON")
  void testSerializeDataCenterToJson() throws Exception {
    DataCenter dc =
        new DataCenter(
            validId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL,
            "Primary facility",
            "us,east");

    String json = objectMapper.writeValueAsString(dc);
    assertTrue(json.contains("\"id\""));
    assertTrue(json.contains("\"name\""));
    assertTrue(json.contains("\"operator\""));
    assertTrue(json.contains("\"coordinates\""));
    assertTrue(json.contains("\"capacity\""));
    assertTrue(json.contains("\"status\""));
    assertTrue(json.contains("\"description\""));
    assertTrue(json.contains("\"tags\""));
  }

  @Test
  @DisplayName("should deserialize JSON to DataCenter")
  void testDeserializeJsonToDataCenter() throws Exception {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "name": "NYC Data Center",
              "operator": "TechCorp",
              "coordinates": {
                "latitude": 40.7128,
                "longitude": -74.0060
              },
              "capacity": 500,
              "status": "operational",
              "description": "Primary facility",
              "tags": "us,east"
            }
            """,
            validId);

    DataCenter dc = objectMapper.readValue(json, DataCenter.class);
    assertEquals(validId, dc.getId());
    assertEquals("NYC Data Center", dc.getName());
    assertEquals("TechCorp", dc.getOperator());
    assertEquals(40.7128, dc.getCoordinates().getLatitude());
    assertEquals(-74.0060, dc.getCoordinates().getLongitude());
    assertEquals(500, dc.getCapacity());
    assertEquals(DataCenterStatus.OPERATIONAL, dc.getStatus());
    assertEquals("Primary facility", dc.getDescription());
    assertEquals("us,east", dc.getTags());
  }

  @Test
  @DisplayName("should handle missing optional fields during deserialization")
  void testDeserializeWithMissingOptionalFields() throws Exception {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "name": "NYC Data Center",
              "operator": "TechCorp",
              "coordinates": {
                "latitude": 40.7128,
                "longitude": -74.0060
              },
              "capacity": 500,
              "status": "operational"
            }
            """,
            validId);

    DataCenter dc = objectMapper.readValue(json, DataCenter.class);
    assertEquals(validId, dc.getId());
    assertEquals("NYC Data Center", dc.getName());
    assertNull(dc.getDescription());
    assertNull(dc.getTags());
  }

  @Test
  @DisplayName("should reject JSON with missing required id field")
  void testRejectMissingIdField() {
    String json =
        """
        {
          "name": "NYC Data Center",
          "operator": "TechCorp",
          "coordinates": {
            "latitude": 40.7128,
            "longitude": -74.0060
          },
          "capacity": 500,
          "status": "operational"
        }
        """;

    assertThrows(Exception.class, () -> objectMapper.readValue(json, DataCenter.class));
  }

  @Test
  @DisplayName("should reject JSON with missing required name field")
  void testRejectMissingNameField() {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "operator": "TechCorp",
              "coordinates": {
                "latitude": 40.7128,
                "longitude": -74.0060
              },
              "capacity": 500,
              "status": "operational"
            }
            """,
            validId);

    assertThrows(Exception.class, () -> objectMapper.readValue(json, DataCenter.class));
  }

  @Test
  @DisplayName("should reject JSON with missing required operator field")
  void testRejectMissingOperatorField() {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "name": "NYC Data Center",
              "coordinates": {
                "latitude": 40.7128,
                "longitude": -74.0060
              },
              "capacity": 500,
              "status": "operational"
            }
            """,
            validId);

    assertThrows(Exception.class, () -> objectMapper.readValue(json, DataCenter.class));
  }

  @Test
  @DisplayName("should reject JSON with missing required coordinates field")
  void testRejectMissingCoordinatesField() {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "name": "NYC Data Center",
              "operator": "TechCorp",
              "capacity": 500,
              "status": "operational"
            }
            """,
            validId);

    assertThrows(Exception.class, () -> objectMapper.readValue(json, DataCenter.class));
  }

  @Test
  @DisplayName("should reject JSON with missing required capacity field")
  void testRejectMissingCapacityField() {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "name": "NYC Data Center",
              "operator": "TechCorp",
              "coordinates": {
                "latitude": 40.7128,
                "longitude": -74.0060
              },
              "status": "operational"
            }
            """,
            validId);

    assertThrows(Exception.class, () -> objectMapper.readValue(json, DataCenter.class));
  }

  @Test
  @DisplayName("should reject JSON with missing required status field")
  void testRejectMissingStatusField() {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "name": "NYC Data Center",
              "operator": "TechCorp",
              "coordinates": {
                "latitude": 40.7128,
                "longitude": -74.0060
              },
              "capacity": 500
            }
            """,
            validId);

    assertThrows(Exception.class, () -> objectMapper.readValue(json, DataCenter.class));
  }

  @Test
  @DisplayName("should handle all valid status values")
  void testAllValidStatusValues() throws Exception {
    for (DataCenterStatus status : DataCenterStatus.values()) {
      String json =
          String.format(
              """
              {
                "id": "%s",
                "name": "Test DC",
                "operator": "TestOp",
                "coordinates": {
                  "latitude": 0.0,
                  "longitude": 0.0
                },
                "capacity": 100,
                "status": "%s"
              }
              """,
              validId, status.getValue());

      DataCenter dc = objectMapper.readValue(json, DataCenter.class);
      assertEquals(status, dc.getStatus());
    }
  }

  @Test
  @DisplayName("should reject invalid status value")
  void testRejectInvalidStatusValue() {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "name": "Test DC",
              "operator": "TestOp",
              "coordinates": {
                "latitude": 0.0,
                "longitude": 0.0
              },
              "capacity": 100,
              "status": "invalid_status"
            }
            """,
            validId);

    assertThrows(Exception.class, () -> objectMapper.readValue(json, DataCenter.class));
  }

  @Test
  @DisplayName("should round-trip serialize and deserialize")
  void testRoundTripSerialization() throws Exception {
    DataCenter original =
        new DataCenter(
            validId,
            "NYC Data Center",
            "TechCorp",
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL,
            "Primary facility",
            "us,east");

    String json = objectMapper.writeValueAsString(original);
    DataCenter deserialized = objectMapper.readValue(json, DataCenter.class);

    assertEquals(original, deserialized);
  }

  @Test
  @DisplayName("should serialize coordinates correctly")
  void testSerializeCoordinatesCorrectly() throws Exception {
    DataCenter dc =
        new DataCenter(
            validId,
            "Test DC",
            "TestOp",
            validCoordinates,
            100,
            DataCenterStatus.OPERATIONAL);

    String json = objectMapper.writeValueAsString(dc);
    ObjectNode node = objectMapper.readValue(json, ObjectNode.class);

    assertEquals(40.7128, node.get("coordinates").get("latitude").asDouble());
    assertEquals(-74.0060, node.get("coordinates").get("longitude").asDouble());
  }

  @Test
  @DisplayName("should handle large capacity values")
  void testHandleLargeCapacityValues() throws Exception {
    long largeCapacity = Long.MAX_VALUE;
    DataCenter dc =
        new DataCenter(
            validId,
            "Test DC",
            "TestOp",
            validCoordinates,
            largeCapacity,
            DataCenterStatus.OPERATIONAL);

    String json = objectMapper.writeValueAsString(dc);
    DataCenter deserialized = objectMapper.readValue(json, DataCenter.class);

    assertEquals(largeCapacity, deserialized.getCapacity());
  }

  @Test
  @DisplayName("should handle special characters in name and operator")
  void testHandleSpecialCharactersInFields() throws Exception {
    String specialName = "Data Center #1 (NYC) - Primary";
    String specialOperator = "Tech & Cloud Corp.";

    DataCenter dc =
        new DataCenter(
            validId,
            specialName,
            specialOperator,
            validCoordinates,
            500,
            DataCenterStatus.OPERATIONAL);

    String json = objectMapper.writeValueAsString(dc);
    DataCenter deserialized = objectMapper.readValue(json, DataCenter.class);

    assertEquals(specialName, deserialized.getName());
    assertEquals(specialOperator, deserialized.getOperator());
  }
}