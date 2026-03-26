package com.datacenter.australia;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AustralianDataCenterValidator Tests")
class AustralianDataCenterValidatorTest {

  private AustralianDataCenterValidator validator;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    validator = new AustralianDataCenterValidator();
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("should validate a valid data center")
  void testValidateValidDataCenter() throws Exception {
    String json =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertTrue(validator.validateDataCenter(node));
    assertEquals(0, validator.getErrorCount());
  }

  @Test
  @DisplayName("should reject data center with invalid UUID")
  void testRejectInvalidUUID() throws Exception {
    String json =
        "{"
            + "\"id\": \"not-a-uuid\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertFalse(validator.validateDataCenter(node));
    assertTrue(validator.getErrorCount() > 0);
  }

  @Test
  @DisplayName("should reject data center outside Australia bounds")
  void testRejectOutsideAustraliaBounds() throws Exception {
    String json =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": 40.7128, \"longitude\": -74.0060},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertFalse(validator.validateDataCenter(node));
    assertTrue(validator.getErrorCount() > 0);
  }

  @Test
  @DisplayName("should reject duplicate IDs")
  void testRejectDuplicateIds() throws Exception {
    String id = UUID.randomUUID().toString();
    String json1 =
        "{"
            + "\"id\": \""
            + id
            + "\","
            + "\"name\": \"Test DC 1\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    String json2 =
        "{"
            + "\"id\": \""
            + id
            + "\","
            + "\"name\": \"Test DC 2\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -37.8136, \"longitude\": 144.9631},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node1 = objectMapper.readTree(json1);
    JsonNode node2 = objectMapper.readTree(json2);

    assertTrue(validator.validateDataCenter(node1));
    assertFalse(validator.validateDataCenter(node2));
  }

  @Test
  @DisplayName("should reject duplicate names")
  void testRejectDuplicateNames() throws Exception {
    String json1 =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    String json2 =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -37.8136, \"longitude\": 144.9631},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node1 = objectMapper.readTree(json1);
    JsonNode node2 = objectMapper.readTree(json2);

    assertTrue(validator.validateDataCenter(node1));
    assertFalse(validator.validateDataCenter(node2));
  }

  @Test
  @DisplayName("should reject unrealistic capacity")
  void testRejectUnrealisticCapacity() throws Exception {
    String json =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 50000,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertFalse(validator.validateDataCenter(node));
  }

  @Test
  @DisplayName("should validate array of data centers")
  void testValidateDataCentersArray() throws Exception {
    String json =
        "["
            + "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC 1\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "},"
            + "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC 2\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -37.8136, \"longitude\": 144.9631},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}"
            + "]";

    JsonNode node = objectMapper.readTree(json);
    assertTrue(validator.validateDataCenters(node));
  }

  @Test
  @DisplayName("should reject non-array root element")
  void testRejectNonArrayRoot() throws Exception {
    String json = "{\"id\": \"" + UUID.randomUUID() + "\"}";
    JsonNode node = objectMapper.readTree(json);
    assertFalse(validator.validateDataCenters(node));
  }

  @Test
  @DisplayName("should clear errors")
  void testClearErrors() throws Exception {
    String json =
        "{"
            + "\"id\": \"invalid\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    validator.validateDataCenter(node);
    assertTrue(validator.getErrorCount() > 0);

    validator.clearErrors();
    assertEquals(0, validator.getErrorCount());
  }

  @Test
  @DisplayName("should convert valid JSON to DataCenter object")
  void testConvertToDataCenter() throws Exception {
    String id = UUID.randomUUID().toString();
    String json =
        "{"
            + "\"id\": \""
            + id
            + "\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\","
            + "\"description\": \"Test description\","
            + "\"tags\": \"test,dc\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    var dc = validator.toDataCenter(node);

    assertEquals(id.toLowerCase(), dc.getId());
    assertEquals("Test DC", dc.getName());
    assertEquals("TestOp", dc.getOperator());
    assertEquals(100, dc.getCapacity());
    assertEquals("Test description", dc.getDescription());
    assertEquals("test,dc", dc.getTags());
  }

  @Test
  @DisplayName("should throw exception when converting invalid JSON")
  void testConvertInvalidThrowsException() throws Exception {
    String json =
        "{"
            + "\"id\": \"invalid\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertThrows(IllegalArgumentException.class, () -> validator.toDataCenter(node));
  }

  @Test
  @DisplayName("should validate Sydney coordinates")
  void testValidateSydneyCoordinates() throws Exception {
    String json =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Sydney DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertTrue(validator.validateDataCenter(node));
  }

  @Test
  @DisplayName("should validate Perth coordinates")
  void testValidatePerthCoordinates() throws Exception {
    String json =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Perth DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -31.9505, \"longitude\": 115.8605},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertTrue(validator.validateDataCenter(node));
  }

  @Test
  @DisplayName("should validate Hobart coordinates")
  void testValidateHobartCoordinates() throws Exception {
    String json =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Hobart DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -42.8821, \"longitude\": 147.3272},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertTrue(validator.validateDataCenter(node));
  }

  @Test
  @DisplayName("should reject coordinates north of Australia")
  void testRejectNorthOfAustralia() throws Exception {
    String json =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -5.0, \"longitude\": 130.0},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertFalse(validator.validateDataCenter(node));
  }

  @Test
  @DisplayName("should reject coordinates south of Australia")
  void testRejectSouthOfAustralia() throws Exception {
    String json =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -50.0, \"longitude\": 130.0},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertFalse(validator.validateDataCenter(node));
  }

  @Test
  @DisplayName("should reject coordinates west of Australia")
  void testRejectWestOfAustralia() throws Exception {
    String json =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -30.0, \"longitude\": 100.0},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertFalse(validator.validateDataCenter(node));
  }

  @Test
  @DisplayName("should reject coordinates east of Australia")
  void testRejectEastOfAustralia() throws Exception {
    String json =
        "{"
            + "\"id\": \""
            + UUID.randomUUID()
            + "\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -30.0, \"longitude\": 160.0},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}";

    JsonNode node = objectMapper.readTree(json);
    assertFalse(validator.validateDataCenter(node));
  }
}