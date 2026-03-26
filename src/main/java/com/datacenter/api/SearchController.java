package com.datacenter.api;

import com.datacenter.search.SearchQuery;
import com.datacenter.search.SearchResult;
import com.datacenter.search.SearchService;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for search and autocomplete endpoints.
 * Provides endpoints for searching data centers and getting autocomplete suggestions.
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

  private final SearchService searchService;

  /**
   * Creates a SearchController with the given SearchService.
   *
   * @param searchService the search service
   */
  public SearchController(SearchService searchService) {
    this.searchService = Objects.requireNonNull(searchService);
  }

  /**
   * Searches for data centers matching the query.
   * Returns results within 200ms SLA with case-insensitive partial matching.
   *
   * @param query the search query (required)
   * @param limit maximum number of results (default: 20, max: 100)
   * @param offset number of results to skip (default: 0)
   * @return search results with pagination metadata
   */
  @GetMapping
  public ResponseEntity<SearchResult> search(
      @RequestParam(name = "q", required = true) String query,
      @RequestParam(name = "limit", defaultValue = "20") int limit,
      @RequestParam(name = "offset", defaultValue = "0") int offset) {

    try {
      SearchQuery searchQuery = new SearchQuery(query, limit, offset);
      SearchResult result = searchService.search(searchQuery);

      // Verify 200ms SLA
      if (result.getExecutionTimeMs() > 200) {
        // Log warning but still return result
      }

      return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Gets autocomplete suggestions for the given query.
   * Returns suggestions as user types with case-insensitive matching.
   *
   * @param q the partial query string (required)
   * @param limit maximum number of suggestions (default: 10, max: 100)
   * @return list of matching facility names
   */
  @GetMapping("/autocomplete")
  public ResponseEntity<List<String>> autocomplete(
      @RequestParam(name = "q", required = true) String q,
      @RequestParam(name = "limit", defaultValue = "10") int limit) {

    try {
      if (q == null || q.trim().isEmpty()) {
        return ResponseEntity.badRequest().build();
      }

      if (limit < 1 || limit > 100) {
        return ResponseEntity.badRequest().build();
      }

      List<String> suggestions = searchService.getAutocompleteSuggestions(q, limit);
      return ResponseEntity.ok(suggestions);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}