package com.datacenter.redis;

import com.datacenter.search.SearchQuery;
import com.datacenter.search.SearchResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;

/**
 * Manages caching of search results and autocomplete suggestions in Redis.
 * Uses 1-hour TTL for all cached data.
 */
public class SearchCacheManager {

  private static final String SEARCH_CACHE_PREFIX = "search:query";
  private static final String AUTOCOMPLETE_CACHE_PREFIX = "search:autocomplete";
  private static final long CACHE_TTL_SECONDS = 3600; // 1 hour

  private final RedisOperations redisOperations;
  private final ObjectMapper objectMapper;

  /**
   * Creates a SearchCacheManager with the given Redis operations and ObjectMapper.
   *
   * @param redisOperations the Redis operations handler
   * @param objectMapper the ObjectMapper for serialization
   */
  public SearchCacheManager(RedisOperations redisOperations, ObjectMapper objectMapper) {
    this.redisOperations = Objects.requireNonNull(redisOperations);
    this.objectMapper = Objects.requireNonNull(objectMapper);
  }

  /**
   * Retrieves a cached search result.
   *
   * @param searchQuery the search query
   * @return the cached SearchResult, or null if not found
   */
  public SearchResult getSearchResult(SearchQuery searchQuery) {
    String key = buildSearchCacheKey(searchQuery);
    String cachedJson = redisOperations.get(key);

    if (cachedJson == null) {
      return null;
    }

    try {
      return objectMapper.readValue(cachedJson, SearchResult.class);
    } catch (Exception e) {
      // Log and return null on deserialization error
      return null;
    }
  }

  /**
   * Caches a search result.
   *
   * @param searchQuery the search query
   * @param result the search result to cache
   */
  public void cacheSearchResult(SearchQuery searchQuery, SearchResult result) {
    String key = buildSearchCacheKey(searchQuery);

    try {
      String json = objectMapper.writeValueAsString(result);
      redisOperations.setWithExpiry(key, json, CACHE_TTL_SECONDS);
    } catch (Exception e) {
      // Log and continue on serialization error
    }
  }

  /**
   * Retrieves cached autocomplete suggestions.
   *
   * @param query the search query
   * @param limit the limit parameter
   * @return the cached suggestions, or null if not found
   */
  public List<String> getAutocompleteSuggestions(String query, int limit) {
    String key = buildAutocompleteCacheKey(query, limit);
    String cachedJson = redisOperations.get(key);

    if (cachedJson == null) {
      return null;
    }

    try {
      return objectMapper.readValue(cachedJson, new TypeReference<List<String>>() {});
    } catch (Exception e) {
      // Log and return null on deserialization error
      return null;
    }
  }

  /**
   * Caches autocomplete suggestions.
   *
   * @param query the search query
   * @param limit the limit parameter
   * @param suggestions the suggestions to cache
   */
  public void cacheAutocompleteSuggestions(String query, int limit, List<String> suggestions) {
    String key = buildAutocompleteCacheKey(query, limit);

    try {
      String json = objectMapper.writeValueAsString(suggestions);
      redisOperations.setWithExpiry(key, json, CACHE_TTL_SECONDS);
    } catch (Exception e) {
      // Log and continue on serialization error
    }
  }

  /**
   * Clears all search-related caches.
   */
  public void clearAllSearchCaches() {
    redisOperations.deleteByPattern(SEARCH_CACHE_PREFIX + ":*");
    redisOperations.deleteByPattern(AUTOCOMPLETE_CACHE_PREFIX + ":*");
  }

  private String buildSearchCacheKey(SearchQuery searchQuery) {
    return String.format(
        "%s:%s:%d:%d",
        SEARCH_CACHE_PREFIX,
        hashQuery(searchQuery.getQuery()),
        searchQuery.getLimit(),
        searchQuery.getOffset());
  }

  private String buildAutocompleteCacheKey(String query, int limit) {
    return String.format("%s:%s:%d", AUTOCOMPLETE_CACHE_PREFIX, hashQuery(query), limit);
  }

  private String hashQuery(String query) {
    // Use a simple hash to avoid key length issues
    return Integer.toHexString(query.toLowerCase().hashCode());
  }
}