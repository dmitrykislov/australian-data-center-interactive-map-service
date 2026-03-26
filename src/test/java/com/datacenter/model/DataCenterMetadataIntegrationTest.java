package com.datacenter.model;

import static org.junit.jupiter.api.Assertions.*;

import com.datacenter.australia.DataCenterMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DataCenter Metadata Integration Tests")
class DataCenterMetadataIntegrationTest {

  private static final String VALID_UUID = "550e8400-e29b-41d4-a716-446655440001";
  private static final Coordinates VALID_COORDS = new Coordinates(-33.8688, 151.2093);
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("should create DataCenter with metadata")
  void testCreateDataCenterWithMetadata() {
    DataCenterMetadata metadata =
        new DataCenterMetadata(
            "Equinix Official Website",
            "confirmed",
            "https://www.equinix.com.au/data-centers/sydney",
            "2026-03-27",
            "NSW",
            "Sydney",
            "Major data center in Sydney CBD");

    DataCenter dc =
        new DataCenter(
            VALID_UUID,
            "Sydney Data Center 1",
            "Equinix",
            VALID_COORDS,
            150,
            DataCenterStatus.OPERATIONAL,
            "Enterprise data center",
            "au,nsw,sydney",
            "confirmed",
            metadata);

    assertNotNull(dc.getMetadata());
    assertEquals("Equinix Official Website", dc.getMetadata().getSourceReference());
    assertEquals("confirmed", dc.getMetadata().getConfirmationStatus());
    assertEquals("https://www.equinix.com.au/data-centers/sydney", dc.getMetadata().getSourceUrl());
    assertEquals("2026-03-27", dc.getMetadata().getLastVerifiedDate());
    assertEquals("NSW", dc.getMetadata().getRegion());
    assertEquals("Sydney", dc.getMetadata().getCity());
    assertEquals("Major data center in Sydney CBD", dc.getMetadata().getComments());
  }

  @Test
  @DisplayName("should create DataCenter without metadata (backward compatibility)")
  void testCreateDataCenterWithoutMetadata() {
    DataCenter dc =
        new DataCenter(
            VALID_UUID,
            "Sydney Data Center 1",
            "Equinix",
            VALID_COORDS,
            150,
            DataCenterStatus.OPERATIONAL,
            "Enterprise data center",
            "au,nsw,sydney");

    assertNull(dc.getMetadata());
  }

  @Test
  @DisplayName("should serialize DataCenter with metadata to JSON")
  void testSerializeDataCenterWithMetadata() throws Exception {
    DataCenterMetadata metadata =
        new DataCenterMetadata(
            "NextDC ASX Filings",
            "confirmed",
            "https://www.nextdc.com/data-centers",
            "2026-03-27",
            "VIC",
            "Melbourne",
            "NextDC facility in Melbourne");

    DataCenter dc =
        new DataCenter(
            VALID_UUID,
            "Melbourne Data Center 1",
            "NextDC",
            VALID_COORDS,
            120,
            DataCenterStatus.OPERATIONAL,
            "Colocation and cloud services",
            "au,vic,melbourne",
            "confirmed",
            metadata);

    String json = objectMapper.writeValueAsString(dc);
    assertTrue(json.contains("\"metadata\""));
    assertTrue(json.contains("\"sourceReference\":\"NextDC ASX Filings\""));
    assertTrue(json.contains("\"region\":\"VIC\""));
    assertTrue(json.contains("\"city\":\"Melbourne\""));
  }

  @Test
  @DisplayName("should deserialize DataCenter with metadata from JSON")
  void testDeserializeDataCenterWithMetadata() throws Exception {
    String json =
        "{"
            + "\"id\":\"550e8400-e29b-41d4-a716-446655440001\","
            + "\"name\":\"Brisbane Data Center 1\","
            + "\"operator\":\"Equinix\","
            + "\"coordinates\":{\"latitude\":-27.4698,\"longitude\":153.0251},"
            + "\"capacity\":80,"
            + "\"status\":\"operational\","
            + "\"description\":\"Equinix facility in Brisbane\","
            + "\"tags\":\"au,qld,brisbane\","
            + "\"confirmationStatus\":\"confirmed\","
            + "\"metadata\":{"
            + "\"sourceReference\":\"Equinix Facility Listings\","
            + "\"confirmationStatus\":\"confirmed\","
            + "\"sourceUrl\":\"https://www.equinix.com.au/data-centers/brisbane\","
            + "\"lastVerifiedDate\":\"2026-03-27\","
            + "\"region\":\"QLD\","
            + "\"city\":\"Brisbane\","
            + "\"comments\":\"Equinix facility serving Queensland\""
            + "}"
            + "}";

    DataCenter dc = objectMapper.readValue(json, DataCenter.class);

    assertNotNull(dc.getMetadata());
    assertEquals("Equinix Facility Listings", dc.getMetadata().getSourceReference());
    assertEquals("confirmed", dc.getMetadata().getConfirmationStatus());
    assertEquals("QLD", dc.getMetadata().getRegion());
    assertEquals("Brisbane", dc.getMetadata().getCity());
  }

  @Test
  @DisplayName("should handle DataCenter with null metadata in JSON")
  void testDeserializeDataCenterWithNullMetadata() throws Exception {
    String json =
        "{"
            + "\"id\":\"550e8400-e29b-41d4-a716-446655440001\","
            + "\"name\":\"Test DC\","
            + "\"operator\":\"TestOp\","
            + "\"coordinates\":{\"latitude\":-33.8688,\"longitude\":151.2093},"
            + "\"capacity\":100,"
            + "\"status\":\"operational\","
            + "\"confirmationStatus\":\"confirmed\","
            + "\"metadata\":null"
            + "}";

    DataCenter dc = objectMapper.readValue(json, DataCenter.class);
    assertNull(dc.getMetadata());
  }

  @Test
  @DisplayName("should maintain equality with metadata")
  void testEqualityWithMetadata() {
    DataCenterMetadata metadata1 =
        new DataCenterMetadata(
            "Source 1", "confirmed", "http://example.com", "2026-03-27", "NSW", "Sydney", "Test");
    DataCenterMetadata metadata2 =
        new DataCenterMetadata(
            "Source 1", "confirmed", "http://example.com", "2026-03-27", "NSW", "Sydney", "Test");

    DataCenter dc1 =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            null,
            null,
            "confirmed",
            metadata1);

    DataCenter dc2 =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            null,
            null,
            "confirmed",
            metadata2);

    assertEquals(dc1, dc2);
  }

  @Test
  @DisplayName("should differ when metadata differs")
  void testInequalityWithDifferentMetadata() {
    DataCenterMetadata metadata1 =
        new DataCenterMetadata(
            "Source 1", "confirmed", "http://example.com", "2026-03-27", "NSW", "Sydney", "Test");
    DataCenterMetadata metadata2 =
        new DataCenterMetadata(
            "Source 2", "unconfirmed", "http://example.com", "2026-03-27", "VIC", "Melbourne", "Test");

    DataCenter dc1 =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            null,
            null,
            "confirmed",
            metadata1);

    DataCenter dc2 =
        new DataCenter(
            VALID_UUID,
            "Test DC",
            "TestOp",
            VALID_COORDS,
            100,
            DataCenterStatus.OPERATIONAL,
            null,
            null,
            "confirmed",
            metadata2);

    assertNotEquals(dc1, dc2);
  }
}