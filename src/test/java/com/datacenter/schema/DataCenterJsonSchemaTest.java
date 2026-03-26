package com.datacenter.schema;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DataCenter JSON Schema Tests")
class DataCenterJsonSchemaTest {

  private DataCenterJsonSchema schema;
  private String validId;

  @BeforeEach
  void setUp() {
    schema = new DataCenterJsonSchema();
    validId = UUID.randomUUID().toString();
  }

  @Test
  @DisplayName("should validate correct DataCenter JSON")
  void testValidateCorrectJson() {
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

    assertTrue(schema.isValid(json));
  }

  @Test
  @DisplayName("should validate JSON with optional fields")
  void testValidateJsonWithOptionalFields() {
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

    assertTrue(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with missing required id")
  void testRejectMissingId() {
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

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with missing required name")
  void testRejectMissingName() {
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

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with missing required operator")
  void testRejectMissingOperator() {
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

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with missing required coordinates")
  void testRejectMissingCoordinates() {
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

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with missing required capacity")
  void testRejectMissingCapacity() {
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

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with missing required status")
  void testRejectMissingStatus() {
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

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with invalid coordinate latitude")
  void testRejectInvalidLatitude() {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "name": "NYC Data Center",
              "operator": "TechCorp",
              "coordinates": {
                "latitude": 91.0,
                "longitude": -74.0060
              },
              "capacity": 500,
              "status": "operational"
            }
            """,
            validId);

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with invalid coordinate longitude")
  void testRejectInvalidLongitude() {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "name": "NYC Data Center",
              "operator": "TechCorp",
              "coordinates": {
                "latitude": 40.7128,
                "longitude": 181.0
              },
              "capacity": 500,
              "status": "operational"
            }
            """,
            validId);

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with zero capacity")
  void testRejectZeroCapacity() {
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
              "capacity": 0,
              "status": "operational"
            }
            """,
            validId);

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with negative capacity")
  void testRejectNegativeCapacity() {
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
              "capacity": -100,
              "status": "operational"
            }
            """,
            validId);

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should reject JSON with invalid status value")
  void testRejectInvalidStatus() {
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
              "status": "invalid_status"
            }
            """,
            validId);

    assertFalse(schema.isValid(json));
  }

  @Test
  @DisplayName("should accept all valid status values")
  void testAcceptAllValidStatuses() {
    String[] validStatuses = {"operational", "maintenance", "planned", "decommissioned"};

    for (String status : validStatuses) {
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
              validId, status);

      assertTrue(schema.isValid(json), "Should accept status: " + status);
    }
  }

  @Test
  @DisplayName("should reject invalid JSON format")
  void testRejectInvalidJsonFormat() {
    String invalidJson = "{ invalid json }";
    assertFalse(schema.isValid(invalidJson));
  }

  @Test
  @DisplayName("should provide validation messages for invalid JSON")
  void testProvideValidationMessages() throws Exception {
    String json =
        String.format(
            """
            {
              "id": "%s",
              "name": "NYC Data Center",
              "operator": "TechCorp",
              "coordinates": {
                "latitude": 91.0,
                "longitude": -74.0060
              },
              "capacity": 500,
              "status": "operational"
            }
            """,
            validId);

    var messages = schema.validate(json);
    assertFalse(messages.isEmpty(), "Should have validation messages");
  }
}