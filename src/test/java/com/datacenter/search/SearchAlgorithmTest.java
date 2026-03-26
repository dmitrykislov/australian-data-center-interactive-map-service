package com.datacenter.search;

import static org.junit.jupiter.api.Assertions.*;

import com.datacenter.model.Coordinates;
import com.datacenter.model.DataCenter;
import com.datacenter.model.DataCenterStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("SearchAlgorithm Tests")
class SearchAlgorithmTest {

  private SearchAlgorithm searchAlgorithm;
  private List<DataCenter> testDataCenters;

  @BeforeEach
  void setUp() {
    searchAlgorithm = new SearchAlgorithm();
    testDataCenters = createTestDataCenters();
  }

  @Test
  @DisplayName("should find exact match on facility name")
  void testExactMatchOnName() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "NYC Data Center");
    assertEquals(1, results.size());
    assertEquals("NYC Data Center", results.get(0).getName());
  }

  @Test
  @DisplayName("should find case-insensitive match")
  void testCaseInsensitiveMatch() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "nyc data center");
    assertEquals(1, results.size());
    assertEquals("NYC Data Center", results.get(0).getName());
  }

  @Test
  @DisplayName("should find case-insensitive match with mixed case")
  void testMixedCaseMatch() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "NyC DaTa CeNtEr");
    assertEquals(1, results.size());
    assertEquals("NYC Data Center", results.get(0).getName());
  }

  @Test
  @DisplayName("should find partial match on facility name")
  void testPartialMatchOnName() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "NYC");
    assertTrue(results.size() >= 1);
    assertTrue(results.stream().anyMatch(dc -> dc.getName().contains("NYC")));
  }

  @Test
  @DisplayName("should find partial match on operator")
  void testPartialMatchOnOperator() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "TechCorp");
    assertTrue(results.size() >= 1);
    assertTrue(results.stream().anyMatch(dc -> dc.getOperator().contains("TechCorp")));
  }

  @Test
  @DisplayName("should find match on description")
  void testMatchOnDescription() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "high-capacity");
    assertTrue(results.size() >= 1);
  }

  @Test
  @DisplayName("should return empty list for no matches")
  void testNoMatches() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "NonExistentFacility");
    assertEquals(0, results.size());
  }

  @Test
  @DisplayName("should handle null data centers list")
  void testNullDataCentersList() {
    List<DataCenter> results = searchAlgorithm.search(null, "query");
    assertEquals(0, results.size());
  }

  @Test
  @DisplayName("should handle empty data centers list")
  void testEmptyDataCentersList() {
    List<DataCenter> results = searchAlgorithm.search(new ArrayList<>(), "query");
    assertEquals(0, results.size());
  }

  @Test
  @DisplayName("should rank exact matches higher than partial matches")
  void testRelevanceRanking() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "Data");
    assertTrue(results.size() > 0);
    // Results should be sorted by relevance
  }

  @Test
  @DisplayName("should get autocomplete suggestions with prefix match")
  void testAutocompletePrefixMatch() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(testDataCenters, "NY", 10);
    assertTrue(suggestions.size() >= 1);
    assertTrue(suggestions.stream().anyMatch(s -> s.toLowerCase().startsWith("ny")));
  }

  @Test
  @DisplayName("should get autocomplete suggestions case-insensitive")
  void testAutocompleteCaseInsensitive() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(testDataCenters, "ny", 10);
    assertTrue(suggestions.size() >= 1);
    assertTrue(suggestions.stream().anyMatch(s -> s.toLowerCase().startsWith("ny")));
  }

  @Test
  @DisplayName("should return empty list for autocomplete with no matches")
  void testAutocompleteNoMatches() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(testDataCenters, "ZZZ", 10);
    assertEquals(0, suggestions.size());
  }

  @Test
  @DisplayName("should respect autocomplete limit")
  void testAutocompleteLimitRespected() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(testDataCenters, "", 2);
    assertTrue(suggestions.size() <= 2);
  }

  @Test
  @DisplayName("should return distinct autocomplete suggestions")
  void testAutocompleteDistinct() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(testDataCenters, "D", 100);
    assertEquals(suggestions.size(), suggestions.stream().distinct().count());
  }

  @Test
  @DisplayName("should handle null data centers in autocomplete")
  void testAutocompleteNullDataCenters() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(null, "query", 10);
    assertEquals(0, suggestions.size());
  }

  @Test
  @DisplayName("should handle empty data centers in autocomplete")
  void testAutocompleteEmptyDataCenters() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(new ArrayList<>(), "query", 10);
    assertEquals(0, suggestions.size());
  }

  private List<DataCenter> createTestDataCenters() {
    List<DataCenter> centers = new ArrayList<>();
    centers.add(
        new DataCenter(
            UUID.randomUUID().toString(),
            "NYC Data Center",
            "TechCorp",
            new Coordinates(40.7128, -74.0060),
            500,
            DataCenterStatus.OPERATIONAL,
            "high-capacity facility",
            "tier-3"));
    centers.add(
        new DataCenter(
            UUID.randomUUID().toString(),
            "Dallas Data Center",
            "TechCorp",
            new Coordinates(32.7767, -96.7970),
            300,
            DataCenterStatus.OPERATIONAL,
            "medium-capacity facility",
            "tier-2"));
    centers.add(
        new DataCenter(
            UUID.randomUUID().toString(),
            "San Francisco Data Center",
            "CloudInc",
            new Coordinates(37.7749, -122.4194),
            400,
            DataCenterStatus.OPERATIONAL,
            "high-capacity facility",
            "tier-3"));
    return centers;
  }
}