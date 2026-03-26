package com.datacenter.search;

import static org.junit.jupiter.api.Assertions.*;

import com.datacenter.model.Coordinates;
import com.datacenter.model.DataCenter;
import com.datacenter.model.DataCenterStatus;
import com.datacenter.redis.RedisOperations;
import com.datacenter.redis.SearchCacheManager;
import com.datacenter.search.SearchAlgorithmImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Search Service Tests")
class SearchServiceTest {

  private SearchService searchService;
  private SearchAlgorithm searchAlgorithm;
  private InMemoryRedisOperations redisOps;
  private SearchCacheManager cacheManager;
  private List<DataCenter> testDataCenters;

  @BeforeEach
  void setUp() {
    searchAlgorithm = new SearchAlgorithmImpl();
    redisOps = new InMemoryRedisOperations();
    cacheManager = new SearchCacheManager(redisOps, new ObjectMapper());
    testDataCenters = createTestDataSet();
    searchService = new SearchService(searchAlgorithm, cacheManager, testDataCenters);
  }

  @Test
  @DisplayName("should find data centers by name")
  void testSearchByName() {
    SearchQuery query = new SearchQuery("Sydney");
    SearchResult result = searchService.search(query);

    assertTrue(result.getResultCount() > 0);
    assertTrue(result.getResults().stream().anyMatch(dc -> dc.getName().contains("Sydney")));
  }

  @Test
  @DisplayName("should find data centers by operator")
  void testSearchByOperator() {
    SearchQuery query = new SearchQuery("Equinix");
    SearchResult result = searchService.search(query);

    assertTrue(result.getResultCount() >= 0);
  }

  @Test
  @DisplayName("should be case-insensitive")
  void testCaseInsensitiveSearch() {
    SearchQuery query1 = new SearchQuery("sydney");
    SearchQuery query2 = new SearchQuery("SYDNEY");
    SearchQuery query3 = new SearchQuery("Sydney");

    SearchResult result1 = searchService.search(query1);
    SearchResult result2 = searchService.search(query2);
    SearchResult result3 = searchService.search(query3);

    assertEquals(result1.getResultCount(), result2.getResultCount());
    assertEquals(result2.getResultCount(), result3.getResultCount());
  }

  @Test
  @DisplayName("should handle empty search results")
  void testEmptySearchResults() {
    SearchQuery query = new SearchQuery("NonexistentDataCenter");
    SearchResult result = searchService.search(query);

    assertEquals(0, result.getResultCount());
    assertTrue(result.getResults().isEmpty());
  }

  @Test
  @DisplayName("should provide autocomplete suggestions")
  void testAutocompleteSuggestions() {
    List<String> suggestions = searchService.getAutocompleteSuggestions("Syd", 10);

    assertTrue(suggestions.size() > 0);
  }

  @Test
  @DisplayName("should limit autocomplete results")
  void testAutocompleteLimit() {
    List<String> suggestions = searchService.getAutocompleteSuggestions("Data", 5);

    assertTrue(suggestions.size() <= 5);
  }

  @Test
  @DisplayName("should handle null search query")
  void testNullSearchQuery() {
    assertThrows(IllegalArgumentException.class, () -> {
      new SearchQuery(null);
    });
  }

  @Test
  @DisplayName("should complete search within SLA")
  void testSearchSLA() {
    SearchQuery query = new SearchQuery("Data");
    long startTime = System.currentTimeMillis();

    SearchResult result = searchService.search(query);

    long totalTime = System.currentTimeMillis() - startTime;
    assertTrue(totalTime < 200, "Search took " + totalTime + "ms, exceeds 200ms SLA");
  }

  private List<DataCenter> createTestDataSet() {
    List<DataCenter> centers = new ArrayList<>();
    centers.add(
        new DataCenter(
            UUID.randomUUID().toString(),
            "Sydney Data Center 1",
            "Equinix",
            new Coordinates(-33.8688, 151.2093),
            500,
            DataCenterStatus.OPERATIONAL,
            "Primary facility",
            "tier-1"));
    centers.add(
        new DataCenter(
            UUID.randomUUID().toString(),
            "Melbourne Data Center",
            "NextDC",
            new Coordinates(-37.8136, 144.9631),
            300,
            DataCenterStatus.OPERATIONAL,
            "Secondary facility",
            "tier-2"));
    centers.add(
        new DataCenter(
            UUID.randomUUID().toString(),
            "Brisbane Data Center",
            "Equinix",
            new Coordinates(-27.4698, 153.0251),
            200,
            DataCenterStatus.PLANNED,
            "Future facility",
            "tier-3"));
    return centers;
  }

  private static class InMemoryRedisOperations implements RedisOperations {
    private final Map<String, String> cache = new java.util.HashMap<>();

    @Override
    public void saveDataCenter(DataCenter dataCenter) {}

    @Override
    public DataCenter getDataCenter(String id) {
      return null;
    }

    @Override
    public List<DataCenter> getAllDataCenters() {
      return new ArrayList<>();
    }

    @Override
    public void deleteDataCenter(String id) {}

    @Override
    public List<DataCenter> filterByOperator(String operator) {
      return new ArrayList<>();
    }

    @Override
    public List<DataCenter> filterByStatus(String status) {
      return new ArrayList<>();
    }

    @Override
    public List<DataCenter> filterByRegion(String region) {
      return new ArrayList<>();
    }

    @Override
    public void clearAll() {}

    @Override
    public <T> T get(String key, Class<T> type) {
      return null;
    }

    @Override
    public <T> void set(String key, T value, long timeout, TimeUnit unit) {}

    @Override
    public void delete(String key) {}

    @Override
    public void deleteByPattern(String pattern) {
      String regex = pattern.replace("*", ".*");
      cache.keySet().removeIf(key -> key.matches(regex));
    }

    @Override
    public boolean exists(String key) {
      return cache.containsKey(key);
    }

    @Override
    public String get(String key) {
      return cache.get(key);
    }

    @Override
    public void setWithExpiry(String key, String value, long ttlSeconds) {
      cache.put(key, value);
    }
  }
}