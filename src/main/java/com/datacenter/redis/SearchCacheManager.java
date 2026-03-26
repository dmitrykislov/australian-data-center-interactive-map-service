package com.datacenter.redis;

import com.datacenter.model.DataCenter;
import com.datacenter.search.SearchResult;
import com.datacenter.validation.InputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Manages search result caching in Redis.
 * Caches search results with a configurable TTL to improve performance.
 */
@Component
public class SearchCacheManager {

    private static final String SEARCH_CACHE_PREFIX = "search:";
    private static final long SEARCH_CACHE_TTL_HOURS = 1;

    private final RedisOperations redisOperations;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public SearchCacheManager(RedisOperations redisOperations, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.redisOperations = redisOperations;
        this.objectMapper = objectMapper;
    }

    /**
     * Gets cached search results for a query.
     *
     * @param query the search query
     * @return cached search results, or null if not found or expired
     */
    public SearchResult getSearchResult(com.datacenter.search.SearchQuery searchQuery) {
        try {
            String cacheKey = SEARCH_CACHE_PREFIX + searchQuery.getQuery().toLowerCase();
            return redisOperations.get(cacheKey, SearchResult.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Caches search results for a query.
     *
     * @param searchQuery the search query
     * @param results the search results to cache
     */
    public void cacheSearchResult(com.datacenter.search.SearchQuery searchQuery, SearchResult results) {
        try {
            String cacheKey = SEARCH_CACHE_PREFIX + searchQuery.getQuery().toLowerCase();
            redisOperations.set(cacheKey, results, SEARCH_CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            // Silently ignore caching errors
        }
    }

    /**
     * Gets cached autocomplete suggestions.
     *
     * @param query the search query
     * @param limit the limit
     * @return cached suggestions, or null if not found or expired
     */
    @SuppressWarnings("unchecked")
    public List<String> getAutocompleteSuggestions(String query, int limit) {
        try {
            String cacheKey = SEARCH_CACHE_PREFIX + "autocomplete:" + query.toLowerCase() + ":" + limit;
            return (List<String>) redisOperations.get(cacheKey, java.util.ArrayList.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Caches autocomplete suggestions.
     *
     * @param query the search query
     * @param limit the limit
     * @param suggestions the suggestions to cache
     */
    public void cacheAutocompleteSuggestions(String query, int limit, List<String> suggestions) {
        try {
            String cacheKey = SEARCH_CACHE_PREFIX + "autocomplete:" + query.toLowerCase() + ":" + limit;
            redisOperations.set(cacheKey, suggestions, SEARCH_CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            // Silently ignore caching errors
        }
    }

    /**
     * Clears all search cache entries.
     */
    public void clearAllSearchCaches() {
        redisOperations.deleteByPattern(SEARCH_CACHE_PREFIX + "*");
    }

    /**
     * Gets the cache key for a search query.
     *
     * @param query the search query
     * @return the cache key
     */
    public String getCacheKey(String query) {
        return SEARCH_CACHE_PREFIX + query.toLowerCase();
    }

    /**
     * Checks if a search result is cached.
     *
     * @param query the search query
     * @return true if cached, false otherwise
     */
    public boolean isCached(String query) {
        try {
            String cacheKey = SEARCH_CACHE_PREFIX + query.toLowerCase();
            return redisOperations.exists(cacheKey);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}