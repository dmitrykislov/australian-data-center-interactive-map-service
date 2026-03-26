package com.datacenter.australia;

import static org.junit.jupiter.api.Assertions.*;

import com.datacenter.model.DataCenter;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AustralianDataCenterLoader Tests")
class AustralianDataCenterLoaderTest {

  private AustralianDataCenterLoader loader;

  @BeforeEach
  void setUp() {
    loader = new AustralianDataCenterLoader();
  }

  @Test
  @DisplayName("should load data centers from classpath")
  void testLoadFromClasspath() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    assertNotNull(dataCenters);
    assertTrue(dataCenters.size() > 0);
  }

  @Test
  @DisplayName("should load at least 15 data centers")
  void testLoadMinimumDataCenters() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();
    assertTrue(dataCenters.size() >= 15, "Should have at least 15 data centers");
  }

  @Test
  @DisplayName("should load data centers with valid IDs")
  void testLoadedDataCentersHaveValidIds() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    for (DataCenter dc : dataCenters) {
      assertNotNull(dc.getId());
      assertFalse(dc.getId().isEmpty());
      // UUID format check
      assertTrue(dc.getId().matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }
  }

  @Test
  @DisplayName("should load data centers with valid names")
  void testLoadedDataCentersHaveValidNames() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    for (DataCenter dc : dataCenters) {
      assertNotNull(dc.getName());
      assertFalse(dc.getName().isEmpty());
      assertTrue(dc.getName().length() <= 255);
    }
  }

  @Test
  @DisplayName("should load data centers with valid operators")
  void testLoadedDataCentersHaveValidOperators() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    for (DataCenter dc : dataCenters) {
      assertNotNull(dc.getOperator());
      assertFalse(dc.getOperator().isEmpty());
      assertTrue(dc.getOperator().length() <= 255);
    }
  }

  @Test
  @DisplayName("should load data centers with valid coordinates")
  void testLoadedDataCentersHaveValidCoordinates() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    for (DataCenter dc : dataCenters) {
      assertNotNull(dc.getCoordinates());
      double lat = dc.getCoordinates().getLatitude();
      double lon = dc.getCoordinates().getLongitude();

      // Australia bounds
      assertTrue(lat >= -43.6 && lat <= -10.0, "Latitude outside Australia: " + lat);
      assertTrue(lon >= 113.0 && lon <= 154.0, "Longitude outside Australia: " + lon);
    }
  }

  @Test
  @DisplayName("should load data centers with valid capacity")
  void testLoadedDataCentersHaveValidCapacity() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    for (DataCenter dc : dataCenters) {
      assertTrue(dc.getCapacity() > 0);
      assertTrue(dc.getCapacity() <= 10000);
    }
  }

  @Test
  @DisplayName("should load data centers with valid status")
  void testLoadedDataCentersHaveValidStatus() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    for (DataCenter dc : dataCenters) {
      assertNotNull(dc.getStatus());
      String status = dc.getStatus().getValue();
      assertTrue(
          status.equals("operational")
              || status.equals("maintenance")
              || status.equals("planned")
              || status.equals("decommissioned"));
    }
  }

  @Test
  @DisplayName("should load data centers with unique IDs")
  void testLoadedDataCentersHaveUniqueIds() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    var ids = dataCenters.stream().map(DataCenter::getId).distinct().count();
    assertEquals(dataCenters.size(), ids, "All data center IDs should be unique");
  }

  @Test
  @DisplayName("should load data centers with unique names")
  void testLoadedDataCentersHaveUniqueNames() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    var names = dataCenters.stream().map(DataCenter::getName).distinct().count();
    assertEquals(dataCenters.size(), names, "All data center names should be unique");
  }

  @Test
  @DisplayName("should load data centers with capital city facilities")
  void testLoadCapitalCityFacilities() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    var capitalCities =
        dataCenters.stream()
            .filter(
                dc ->
                    dc.getName().toLowerCase().contains("sydney")
                        || dc.getName().toLowerCase().contains("melbourne")
                        || dc.getName().toLowerCase().contains("brisbane")
                        || dc.getName().toLowerCase().contains("perth")
                        || dc.getName().toLowerCase().contains("adelaide")
                        || dc.getName().toLowerCase().contains("hobart")
                        || dc.getName().toLowerCase().contains("canberra")
                        || dc.getName().toLowerCase().contains("darwin"))
            .count();

    assertTrue(capitalCities >= 8, "Should have data centers in all capital cities");
  }

  @Test
  @DisplayName("should load data centers with regional facilities")
  void testLoadRegionalFacilities() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    var regionalCount =
        dataCenters.stream()
            .filter(
                dc ->
                    dc.getName().toLowerCase().contains("newcastle")
                        || dc.getName().toLowerCase().contains("geelong")
                        || dc.getName().toLowerCase().contains("wollongong")
                        || dc.getName().toLowerCase().contains("townsville")
                        || dc.getName().toLowerCase().contains("cairns")
                        || dc.getName().toLowerCase().contains("launceston")
                        || dc.getName().toLowerCase().contains("gold coast"))
            .count();

    assertTrue(regionalCount >= 5, "Should have regional data centers");
  }

  @Test
  @DisplayName("should load data centers with defence facilities")
  void testLoadDefenceFacilities() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    var defenceCount =
        dataCenters.stream()
            .filter(
                dc ->
                    dc.getName().toLowerCase().contains("defence")
                        || dc.getOperator().toLowerCase().contains("defence")
                        || dc.getOperator().toLowerCase().contains("military"))
            .count();

    assertTrue(defenceCount >= 1, "Should have at least one defence facility");
  }

  @Test
  @DisplayName("should load data centers with confirmed status")
  void testLoadConfirmedDataCenters() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    var confirmedCount = dataCenters.stream().filter(dc -> dc.getDescription() != null).count();

    assertTrue(confirmedCount > 0, "Should have data centers with descriptions");
  }

  @Test
  @DisplayName("should load data centers with operational status")
  void testLoadOperationalDataCenters() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    var operationalCount =
        dataCenters.stream()
            .filter(dc -> dc.getStatus().getValue().equals("operational"))
            .count();

    assertTrue(operationalCount >= 10, "Should have at least 10 operational data centers");
  }

  @Test
  @DisplayName("should load data centers with planned status")
  void testLoadPlannedDataCenters() throws IOException {
    List<DataCenter> dataCenters = loader.loadFromClasspath();

    var plannedCount =
        dataCenters.stream()
            .filter(dc -> dc.getStatus().getValue().equals("planned"))
            .count();

    assertTrue(plannedCount >= 1, "Should have at least one planned data center");
  }

  @Test
  @DisplayName("should throw exception for missing file")
  void testThrowExceptionForMissingFile() {
    assertThrows(IOException.class, () -> loader.loadFromFile(java.nio.file.Paths.get("/nonexistent/path")));
  }

  @Test
  @DisplayName("should load from valid JSON string")
  void testLoadFromValidJsonString() throws IOException {
    String json =
        "["
            + "{"
            + "\"id\": \"550e8400-e29b-41d4-a716-446655440001\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}"
            + "]";

    List<DataCenter> dataCenters = loader.loadFromString(json);
    assertEquals(1, dataCenters.size());
  }

  @Test
  @DisplayName("should throw exception for invalid JSON string")
  void testThrowExceptionForInvalidJsonString() {
    String json = "invalid json";
    assertThrows(IOException.class, () -> loader.loadFromString(json));
  }

  @Test
  @DisplayName("should throw exception for invalid data in JSON string")
  void testThrowExceptionForInvalidDataInJsonString() {
    String json =
        "["
            + "{"
            + "\"id\": \"invalid-uuid\","
            + "\"name\": \"Test DC\","
            + "\"operator\": \"TestOp\","
            + "\"coordinates\": {\"latitude\": -33.8688, \"longitude\": 151.2093},"
            + "\"capacity\": 100,"
            + "\"status\": \"operational\""
            + "}"
            + "]";

    assertThrows(IllegalArgumentException.class, () -> loader.loadFromString(json));
  }
}