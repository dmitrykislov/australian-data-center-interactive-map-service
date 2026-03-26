package com.datacenter.search;

import com.datacenter.model.DataCenter;
import com.datacenter.redis.SearchCacheManager;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

/**
 * Service for searching data centers with caching support.
 * Provides search and autocomplete functionality with Redis caching (1-hour TTL).
 */
@Service
public class SearchService {

  private final SearchAlgorithm searchAlgorithm;
  private final SearchCacheManager cacheManager;
  private final List<DataCenter> dataCenters;

  /**
   * Creates a SearchService with the given algorithm, cache manager, and data centers.
   *
   * @param searchAlgorithm the search algorithm implementation
   * @param cacheManager the cache manager for storing results
   * @param dataCenters the list of data centers to search
   */
  public SearchService(
      SearchAlgorithm searchAlgorithm,
      SearchCacheManager cacheManager,
      List<DataCenter> dataCenters) {
    this.searchAlgorithm = Objects.requireNonNull(searchAlgorithm);
    this.cacheManager = Objects.requireNonNull(cacheManager);
    this.dataCenters = Objects.requireNonNull(dataCenters);
  }

  /**
   * Searches for data centers matching the query.
   * Results are cached in Redis with 1-hour TTL.
   *
   * @param searchQuery the search query with pagination parameters
   * @return search results with execution time
   */
  public SearchResult search(SearchQuery searchQuery) {
    Objects.requireNonNull(searchQuery);

    // Check cache first
    SearchResult cachedResult = cacheManager.getSearchResult(searchQuery);
    if (cachedResult != null) {
      return cachedResult;
    }

    long startTime = System.currentTimeMillis();

    // Perform search
    List<DataCenter> allMatches = searchAlgorithm.search(dataCenters, searchQuery.getQuery());

    // Apply pagination
    int totalCount = allMatches.size();
    List<DataCenter> paginatedResults =
        allMatches.stream()
            .skip(searchQuery.getOffset())
            .limit(searchQuery.getLimit())
            .toList();

    long executionTimeMs = System.currentTimeMillis() - startTime;

    SearchResult result =
        new SearchResult(
            paginatedResults,
            totalCount,
            searchQuery.getLimit(),
            searchQuery.getOffset(),
            executionTimeMs);

    // Cache the result
    cacheManager.cacheSearchResult(searchQuery, result);

    return result;
  }

  /**
   * Gets autocomplete suggestions for the given query.
   * Results are cached in Redis with 1-hour TTL.
   *
   * @param query the partial query string
   * @param limit maximum number of suggestions
   * @return list of matching facility names
   */
  public List<String> getAutocompleteSuggestions(String query, int limit) {
    if (query == null || query.trim().isEmpty()) {
      return List.of();
    }

    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException(
          String.format("Limit must be between 1 and 100, got %d", limit));
    }

    // Check cache first
    List<String> cachedSuggestions = cacheManager.getAutocompleteSuggestions(query, limit);
    if (cachedSuggestions != null) {
      return cachedSuggestions;
    }

    // Get suggestions
    List<String> suggestions =
        searchAlgorithm.getAutocompleteSuggestions(dataCenters, query, limit);

    // Cache the suggestions
    cacheManager.cacheAutocompleteSuggestions(query, limit, suggestions);

    return suggestions;
  }

  /**
   * Clears all search caches.
   * Should be called when data centers are updated.
   */
  public void clearCache() {
    cacheManager.clearAllSearchCaches();
  }
}