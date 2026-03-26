package com.datacenter.search;

import static org.junit.jupiter.api.Assertions.*;

import com.datacenter.model.Coordinates;
import com.datacenter.model.DataCenter;
import com.datacenter.model.DataCenterStatus;
import com.datacenter.redis.RedisOperations;
import com.datacenter.redis.SearchCacheManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Search Integration Tests")
class SearchIntegrationTest {

  private SearchService searchService;
  private SearchAlgorithm searchAlgorithm;
  private MockRedisOperations mockRedisOps;
  private SearchCacheManager cacheManager;
  private List<DataCenter> testDataCenters;

  @BeforeEach
  void setUp() {
    searchAlgorithm = new SearchAlgorithm();
    mockRedisOps = new MockRedisOperations();
    cacheManager = new SearchCacheManager(mockRedisOps, new ObjectMapper());
    testDataCenters = createLargeTestDataSet();
    searchService = new SearchService(searchAlgorithm, cacheManager, testDataCenters);
  }

  @Test
  @DisplayName("should complete search within 200ms SLA")
  void testSearchCompletesWithin200ms() {
    SearchQuery query = new SearchQuery("Data Center");
    long startTime = System.currentTimeMillis();

    SearchResult result = searchService.search(query);

    long totalTime = System.currentTimeMillis() - startTime;
    assertTrue(totalTime < 200, "Search took " + totalTime + "ms, exceeds 200ms SLA");
    assertTrue(result.getExecutionTimeMs() < 200);
  }

  @Test
  @DisplayName("should cache search results")
  void testSearchResultsCached() {
    SearchQuery query = new SearchQuery("NYC");

    // First search - should not be cached
    SearchResult result1 = searchService.search(query);
    long time1 = result1.getExecutionTimeMs();

    // Second search - should be cached and faster
    SearchResult result2 = searchService.search(query);
    long time2 = result2.getExecutionTimeMs();

    // Results should be identical
    assertEquals(result1.getTotalCount(), result2.getTotalCount());
    assertEquals(result1.getResultCount(), result2.getResultCount());
  }

  @Test
  @DisplayName("should cache autocomplete suggestions")
  void testAutocompleteSuggestionsCached() {
    // First call
    List<String> suggestions1 = searchService.getAutocompleteSuggestions("NY", 10);

    // Second call - should be cached
    List<String> suggestions2 = searchService.getAutocompleteSuggestions("NY", 10);

    assertEquals(suggestions1, suggestions2);
  }

  @Test
  @DisplayName("should handle large dataset efficiently")
  void testLargeDatasetSearch() {
    SearchQuery query = new SearchQuery("Center");
    long startTime = System.currentTimeMillis();

    SearchResult result = searchService.search(query);

    long totalTime = System.currentTimeMillis() - startTime;
    assertTrue(totalTime < 200, "Large dataset search took " + totalTime + "ms");
    assertTrue(result.getTotalCount() > 0);
  }

  @Test
  @DisplayName("should clear cache on demand")
  void testClearCache() {
    SearchQuery query = new SearchQuery("NYC");
    searchService.search(query);

    assertTrue(mockRedisOps.hasKeys());

    searchService.clearCache();

    assertTrue(mockRedisOps.isEmpty());
  }

  @Test
  @DisplayName("should handle concurrent searches")
  void testConcurrentSearches() throws InterruptedException {
    List<Thread> threads = new ArrayList<>();
    List<SearchResult> results = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      Thread thread =
          new Thread(
              () -> {
                SearchQuery query = new SearchQuery("Data");
                SearchResult result = searchService.search(query);
                synchronized (results) {
                  results.add(result);
                }
              });
      threads.add(thread);
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    assertEquals(5, results.size());
    // All results should be identical
    SearchResult first = results.get(0);
    for (SearchResult result : results) {
      assertEquals(first.getTotalCount(), result.getTotalCount());
    }
  }

  private List<DataCenter> createLargeTestDataSet() {
    List<DataCenter> centers = new ArrayList<>();
    String[] cities = {"NYC", "Dallas", "San Francisco", "Chicago", "Boston", "Seattle", "Denver"};
    String[] operators = {"TechCorp", "CloudInc", "DataFlow", "NetWorks", "InfoSys"};

    for (int i = 0; i < 100; i++) {
      String city = cities[i % cities.length];
      String operator = operators[i % operators.length];
      centers.add(
          new DataCenter(
              UUID.randomUUID().toString(),
              city + " Data Center " + i,
              operator,
              new Coordinates(40.0 + (i % 10), -74.0 + (i % 10)),
              100 + (i * 10),
              DataCenterStatus.OPERATIONAL,
              "Test facility " + i,
              "tier-" + (1 + (i % 3))));
    }
    return centers;
  }

  /**
   * Mock Redis operations for testing without actual Redis.
   */
  private static class MockRedisOperations implements RedisOperations {
    private final java.util.Map<String, String> store = new java.util.HashMap<>();

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

    boolean hasKeys() {
      return !store.isEmpty();
    }

    boolean isEmpty() {
      return store.isEmpty();
    }
  }
}