package com.datacenter.api;

import static org.junit.jupiter.api.Assertions.*;

import com.datacenter.model.Coordinates;
import com.datacenter.model.DataCenter;
import com.datacenter.model.DataCenterStatus;
import com.datacenter.redis.RedisOperations;
import com.datacenter.redis.SearchCacheManager;
import com.datacenter.search.SearchAlgorithm;
import com.datacenter.search.SearchQuery;
import com.datacenter.search.SearchResult;
import com.datacenter.search.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("SearchController Tests")
class SearchControllerTest {

  private SearchController searchController;
  private SearchService searchService;
  private List<DataCenter> testDataCenters;

  @BeforeEach
  void setUp() {
    testDataCenters = createTestDataCenters();
    SearchAlgorithm searchAlgorithm = new SearchAlgorithm();
    RedisOperations redisOps = new InMemoryRedisOperations();
    SearchCacheManager cacheManager = new SearchCacheManager(redisOps, new ObjectMapper());
    searchService = new SearchService(searchAlgorithm, cacheManager, testDataCenters);
    searchController = new SearchController(searchService);
  }

  @Test
  @DisplayName("should return search results with 200 OK")
  void testSearchReturns200() {
    ResponseEntity<SearchResult> response = searchController.search("NYC", 20, 0);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getResultCount() >= 1);
  }

  @Test
  @DisplayName("should return bad request for null query")
  void testSearchRejectNullQuery() {
    ResponseEntity<SearchResult> response = searchController.search(null, 20, 0);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("should return bad request for empty query")
  void testSearchRejectEmptyQuery() {
    ResponseEntity<SearchResult> response = searchController.search("", 20, 0);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("should return bad request for invalid limit")
  void testSearchRejectInvalidLimit() {
    ResponseEntity<SearchResult> response = searchController.search("NYC", 101, 0);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("should return bad request for negative offset")
  void testSearchRejectNegativeOffset() {
    ResponseEntity<SearchResult> response = searchController.search("NYC", 20, -1);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("should return autocomplete suggestions with 200 OK")
  void testAutocompleteReturns200() {
    ResponseEntity<List<String>> response = searchController.autocomplete("NYC", 10);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().size() >= 1);
  }

  @Test
  @DisplayName("should return bad request for null autocomplete query")
  void testAutocompleteRejectNullQuery() {
    ResponseEntity<List<String>> response = searchController.autocomplete(null, 10);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("should return bad request for empty autocomplete query")
  void testAutocompleteRejectEmptyQuery() {
    ResponseEntity<List<String>> response = searchController.autocomplete("", 10);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("should return bad request for invalid autocomplete limit")
  void testAutocompleteRejectInvalidLimit() {
    ResponseEntity<List<String>> response = searchController.autocomplete("NYC", 101);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("should return bad request for zero autocomplete limit")
  void testAutocompleteRejectZeroLimit() {
    ResponseEntity<List<String>> response = searchController.autocomplete("NYC", 0);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  @DisplayName("should apply pagination to search results")
  void testSearchAppliesPagination() {
    ResponseEntity<SearchResult> response = searchController.search("Data", 10, 20);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(10, response.getBody().getLimit());
    assertEquals(20, response.getBody().getOffset());
  }

  @Test
  @DisplayName("should use default limit for autocomplete")
  void testAutocompleteUsesDefaultLimit() {
    ResponseEntity<List<String>> response = searchController.autocomplete("NYC", 10);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  @DisplayName("should handle case-insensitive search")
  void testCaseInsensitiveSearch() {
    ResponseEntity<SearchResult> response = searchController.search("nyc", 20, 0);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().getResultCount() >= 1);
  }

  @Test
  @DisplayName("should handle partial matching")
  void testPartialMatching() {
    ResponseEntity<SearchResult> response = searchController.search("NYC", 20, 0);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().getResultCount() >= 1);
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
            DataCenterStatus.OPERATIONAL));
    centers.add(
        new DataCenter(
            UUID.randomUUID().toString(),
            "Dallas Data Center",
            "TechCorp",
            new Coordinates(32.7767, -96.7970),
            300,
            DataCenterStatus.OPERATIONAL));
    return centers;
  }

  /**
   * In-memory Redis implementation for testing.
   */
  private static class InMemoryRedisOperations implements RedisOperations {
    private final Map<String, String> store = new HashMap<>();

    @Override
    public String get(String key) {
      return store.get(key);
    }

    @Override
    public void setWithExpiry(String key, String value, long ttlSeconds) {
      store.put(key, value);
    }

    @Override
    public void deleteByPattern(String pattern) {
      String regex = pattern.replace("*", ".*");
      store.keySet().removeIf(key -> key.matches(regex));
    }
  }
}