package com.datacenter.search;

import static org.junit.jupiter.api.Assertions.*;

import com.datacenter.model.Coordinates;
import com.datacenter.model.DataCenter;
import com.datacenter.model.DataCenterStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SearchAlgorithmImplTest {

  private SearchAlgorithmImpl searchAlgorithm;
  private List<DataCenter> testDataCenters;

  @BeforeEach
  void setUp() {
    searchAlgorithm = new SearchAlgorithmImpl();
    testDataCenters = new ArrayList<>();

    testDataCenters.add(
        new DataCenter(
            UUID.randomUUID().toString(),
            "Sydney Data Center",
            "Equinix",
            new Coordinates(-33.8688, 151.2093),
            1000,
            DataCenterStatus.OPERATIONAL));

    testDataCenters.add(
        new DataCenter(
            UUID.randomUUID().toString(),
            "Melbourne Data Center",
            "NextDC",
            new Coordinates(-37.8136, 144.9631),
            800,
            DataCenterStatus.OPERATIONAL));

    testDataCenters.add(
        new DataCenter(
            UUID.randomUUID().toString(),
            "Brisbane Data Center",
            "Equinix",
            new Coordinates(-27.4698, 153.0251),
            600,
            DataCenterStatus.PLANNED));
  }

  @Test
  void testSearchByName() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "Sydney");
    assertEquals(1, results.size());
    assertEquals("Sydney Data Center", results.get(0).getName());
  }

  @Test
  void testSearchByOperator() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "Equinix");
    assertEquals(2, results.size());
  }

  @Test
  void testCaseInsensitiveSearch() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "SYDNEY");
    assertEquals(1, results.size());
    assertEquals("Sydney Data Center", results.get(0).getName());
  }

  @Test
  void testPartialMatchSearch() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "Data");
    assertEquals(3, results.size());
  }

  @Test
  void testEmptyQuery() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "");
    assertEquals(0, results.size());
  }

  @Test
  void testNullQuery() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, null);
    assertEquals(0, results.size());
  }

  @Test
  void testNullDataCenters() {
    List<DataCenter> results = searchAlgorithm.search(null, "Sydney");
    assertEquals(0, results.size());
  }

  @Test
  void testEmptyDataCenters() {
    List<DataCenter> results = searchAlgorithm.search(new ArrayList<>(), "Sydney");
    assertEquals(0, results.size());
  }

  @Test
  void testRelevanceScoring() {
    List<DataCenter> results = searchAlgorithm.search(testDataCenters, "Sydney");
    assertEquals(1, results.size());
    assertEquals("Sydney Data Center", results.get(0).getName());
  }

  @Test
  void testAutocompleteSuggestions() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(testDataCenters, "Syd", 10);
    assertEquals(1, suggestions.size());
    assertEquals("Sydney Data Center", suggestions.get(0));
  }

  @Test
  void testAutocompleteLimit() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(testDataCenters, "Syd", 1);
    assertEquals(1, suggestions.size());
  }

  @Test
  void testAutocompleteCaseInsensitive() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(testDataCenters, "mel", 10);
    assertEquals(1, suggestions.size());
    assertEquals("Melbourne Data Center", suggestions.get(0));
  }

  @Test
  void testAutocompleteNoMatch() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(testDataCenters, "xyz", 10);
    assertEquals(0, suggestions.size());
  }

  @Test
  void testAutocompleteNullDataCenters() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(null, "Syd", 10);
    assertEquals(0, suggestions.size());
  }

  @Test
  void testAutocompleteEmptyDataCenters() {
    List<String> suggestions = searchAlgorithm.getAutocompleteSuggestions(new ArrayList<>(), "Syd", 10);
    assertEquals(0, suggestions.size());
  }
}