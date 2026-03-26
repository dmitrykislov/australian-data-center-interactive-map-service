package com.datacenter.api;

import com.datacenter.search.SearchQuery;
import com.datacenter.search.SearchResult;
import com.datacenter.search.SearchService;
import com.datacenter.validation.InputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST API controller for search endpoints.
 * Provides search and autocomplete functionality for data centers.
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

  private final SearchService searchService;
  private final InputValidator inputValidator;

  /**
   * Creates a SearchController with the given dependencies.
   *
   * @param searchService the search service
   * @param inputValidator the input validator
   */
  @Autowired
  public SearchController(SearchService searchService, InputValidator inputValidator) {
    this.searchService = Objects.requireNonNull(searchService);
    this.inputValidator = Objects.requireNonNull(inputValidator);
  }

  /**
   * Searches data centers by query string.
   *
   * @param query the search query
   * @param limit the maximum number of results (default: 20, max: 100)
   * @param offset the offset for pagination (default: 0)
   * @return search results
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> search(
      @RequestParam(name = "q", defaultValue = "") String query,
      @RequestParam(name = "limit", defaultValue = "20") int limit,
      @RequestParam(name = "offset", defaultValue = "0") int offset) {
    // Validate parameters - limit must be 1-100 for SearchQuery
    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException("Limit must be between 1 and 100");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must be non-negative");
    }

    // Empty queries are allowed - no validation needed for empty string
    // Only validate non-empty queries
    if (query != null && !query.isEmpty() && !query.trim().isEmpty()) {
      inputValidator.validateSearchQuery(query);
    }

    // Perform search
    SearchQuery searchQuery = new SearchQuery(query, limit, offset);
    SearchResult result = searchService.search(searchQuery);

    Map<String, Object> response = new HashMap<>();
    response.put("results", result.getResults());
    response.put("total", result.getResults().size());
    response.put("limit", limit);
    response.put("offset", offset);

    return ResponseEntity.ok(response);
  }

  /**
   * Provides autocomplete suggestions for search queries.
   *
   * @param query the partial search query
   * @param limit the maximum number of suggestions (default: 10)
   * @return autocomplete suggestions
   */
  @GetMapping("/autocomplete")
  public ResponseEntity<Map<String, Object>> autocomplete(
      @RequestParam(name = "q", defaultValue = "") String query,
      @RequestParam(name = "limit", defaultValue = "10") int limit) {
    // Validate parameters - limit must be 1-100 for SearchQuery
    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException("Limit must be between 1 and 100");
    }

    // Empty queries are allowed - no validation needed for empty string
    // Only validate non-empty queries
    if (query != null && !query.isEmpty() && !query.trim().isEmpty()) {
      inputValidator.validateSearchQuery(query);
    }

    // Get autocomplete suggestions using search
    SearchQuery searchQuery = new SearchQuery(query, limit, 0);
    SearchResult result = searchService.search(searchQuery);
    
    // Extract unique names/operators as suggestions
    List<String> suggestions = result.getResults().stream()
        .map(dc -> dc.getName())
        .distinct()
        .limit(limit)
        .toList();

    Map<String, Object> response = new HashMap<>();
    response.put("suggestions", suggestions);
    response.put("query", query);

    return ResponseEntity.ok(response);
  }
}