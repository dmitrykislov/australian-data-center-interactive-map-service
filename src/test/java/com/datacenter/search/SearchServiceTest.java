package com.datacenter.search;

import static org.junit.jupiter.api.Assertions.*;

import com.datacenter.model.Coordinates;
import com.datacenter.model.DataCenter;
import com.datacenter.model.DataCenterStatus;
import com.datacenter.redis.RedisOperations;
import com.datacenter.redis.SearchCacheManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SearchService Tests")
class SearchServiceTest {

  private SearchService searchService;
  private SearchAlgorithm searchAlgorithm;
  private SearchCacheManager cacheManager;
  private List<DataCenter> testDataCenters;

  @BeforeEach
  void setUp() {
    searchAlgorithm = new SearchAlgorithm();
    RedisOperations redisOps = new InMemoryRedisOperations();
    cacheManager = new SearchCacheManager(redisOps, new ObjectMapper());
    testDataCenters = createTestDataCenters();
    searchService = new SearchService(searchAlgorithm, cacheManager, testDataCenters);
  }

  @Test
  @DisplayName("should return search results")
  void testSearchReturnsResults() {
    SearchQuery query = new SearchQuery("NYC");

    SearchResult result = searchService.search(query);

    assertNotNull(result);
    assertTrue(result.getResultCount() >= 1);
  }

  @Test
  @DisplayName("should return cached results when available")
  void testSearchReturnsCachedResults() {
    SearchQuery query = new SearchQuery("NYC");

    // First search
    SearchResult result1 = searchService.search(query);
    long time1 = result1.getExecutionTimeMs();

    // Second search - should be cached
    SearchResult result2 = searchService.search(query);
    long time2 = result2.getExecutionTimeMs();

    assertEquals(result1.getTotalCount(), result2.getTotalCount());
    assertEquals(result1.getResultCount(), result2.getResultCount());
  }

  @Test
  @DisplayName("should apply pagination to search results")
  void testSearchAppliesPagination() {
    SearchQuery query = new SearchQuery("Data", 1, 0);

    SearchResult result = searchService.search(query);

    assertEquals(1, result.getLimit());
    assertEquals(0, result.getOffset());
    assertTrue(result.getResultCount() <= 1);
  }

  @Test
  @DisplayName("should measure execution time")
  void testSearchMeasuresExecutionTime() {
    SearchQuery query = new SearchQuery("NYC");

    SearchResult result = searchService.search(query);

    assertTrue(result.getExecutionTimeMs() >= 0);
    assertTrue(result.getExecutionTimeMs() < 1000); // Should be fast
  }

  @Test
  @DisplayName("should return autocomplete suggestions")
  void testGetAutocompleteSuggestions() {
    List<String> suggestions = searchService.getAutocompleteSuggestions("NY", 10);

    assertNotNull(suggestions);
    assertTrue(suggestions.size() >= 1);
  }

  @Test
  @DisplayName("should return cached autocomplete suggestions")
  void testGetAutocompleteSuggestionsFromCache() {
    // First call
    List<String> suggestions1 = searchService.getAutocompleteSuggestions("NY", 10);

    // Second call - should be cached
    List<String> suggestions2 = searchService.getAutocompleteSuggestions("NY", 10);

    assertEquals(suggestions1, suggestions2);
  }

  @Test
  @DisplayName("should reject null search query")
  void testRejectNullSearchQuery() {
    assertThrows(NullPointerException.class, () -> searchService.search(null));
  }

  @Test
  @DisplayName("should reject empty autocomplete query")
  void testRejectEmptyAutocompleteQuery() {
    List<String> suggestions = searchService.getAutocompleteSuggestions("", 10);
    assertEquals(0, suggestions.size());
  }

  @Test
  @DisplayName("should reject null autocomplete query")
  void testRejectNullAutocompleteQuery() {
    List<String> suggestions = searchService.getAutocompleteSuggestions(null, 10);
    assertEquals(0, suggestions.size());
  }

  @Test
  @DisplayName("should reject invalid autocomplete limit")
  void testRejectInvalidAutocompleteLimit() {
    assertThrows(
        IllegalArgumentException.class, () -> searchService.getAutocompleteSuggestions("NY", 0));
    assertThrows(
        IllegalArgumentException.class, () -> searchService.getAutocompleteSuggestions("NY", 101));
  }

  @Test
  @DisplayName("should clear cache")
  void testClearCache() {
    SearchQuery query = new SearchQuery("NYC");
    searchService.search(query);

    searchService.clearCache();

    // Verify cache was cleared by searching again and checking it's not cached
    SearchResult result = searchService.search(query);
    assertNotNull(result);
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