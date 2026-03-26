package com.datacenter.api;

import com.datacenter.search.SearchQuery;
import com.datacenter.search.SearchResult;
import com.datacenter.search.SearchService;
import com.datacenter.validation.InputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * REST API controller for data center endpoints.
 * Provides endpoints for retrieving and filtering data center information.
 */
@RestController
@RequestMapping("/api/v1/datacenters")
public class DataCenterController {

  private final SearchService searchService;
  private final InputValidator inputValidator;

  /**
   * Creates a DataCenterController with the given dependencies.
   *
   * @param searchService the search service
   * @param inputValidator the input validator
   */
  @Autowired
  public DataCenterController(SearchService searchService, InputValidator inputValidator) {
    this.searchService = Objects.requireNonNull(searchService);
    this.inputValidator = Objects.requireNonNull(inputValidator);
  }

  /**
   * Gets all data centers with pagination support.
   *
   * @param limit the maximum number of results (default: 100, max: 1000)
   * @param offset the offset for pagination (default: 0)
   * @return list of data centers
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllDataCenters(
      @RequestParam(name = "limit", defaultValue = "100") int limit,
      @RequestParam(name = "offset", defaultValue = "0") int offset) {
    // Validate parameters - limit must be 1-100 for SearchQuery
    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException("Limit must be between 1 and 100");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must be non-negative");
    }

    // Use search with empty query to get all data centers
    SearchQuery query = new SearchQuery("", limit, offset);
    SearchResult result = searchService.search(query);

    Map<String, Object> response = new HashMap<>();
    response.put("dataCenters", result.getResults());
    response.put("total", result.getResults().size());
    response.put("limit", limit);
    response.put("offset", offset);

    return ResponseEntity.ok(response);
  }

  /**
   * Gets a specific data center by ID.
   *
   * @param id the data center UUID
   * @return the data center details
   */
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getDataCenterById(@PathVariable String id) {
    inputValidator.validateUUID(id);

    // Search for data center by ID
    SearchQuery query = new SearchQuery(id, 1, 0);
    SearchResult result = searchService.search(query);

    if (result.getResults().isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Map<String, Object> response = new HashMap<>();
    response.put("dataCenter", result.getResults().get(0));
    return ResponseEntity.ok(response);
  }

  /**
   * Filters data centers by operator.
   *
   * @param operator the operator name
   * @param limit the maximum number of results
   * @return filtered list of data centers
   */
  @GetMapping("/filter/operator")
  public ResponseEntity<Map<String, Object>> filterByOperator(
      @RequestParam(name = "operator") String operator,
      @RequestParam(name = "limit", defaultValue = "100") int limit) {
    // Validate parameters - limit must be 1-100 for SearchQuery
    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException("Limit must be between 1 and 100");
    }
    inputValidator.validateOperator(operator);

    SearchQuery query = new SearchQuery(operator, limit, 0);
    SearchResult result = searchService.search(query);

    Map<String, Object> response = new HashMap<>();
    response.put("dataCenters", result.getResults());
    response.put("total", result.getResults().size());
    return ResponseEntity.ok(response);
  }

  /**
   * Filters data centers by status.
   *
   * @param status the status (operational or planned)
   * @param limit the maximum number of results
   * @return filtered list of data centers
   */
  @GetMapping("/filter/status")
  public ResponseEntity<Map<String, Object>> filterByStatus(
      @RequestParam(name = "status") String status,
      @RequestParam(name = "limit", defaultValue = "100") int limit) {
    // Validate parameters - limit must be 1-100 for SearchQuery
    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException("Limit must be between 1 and 100");
    }
    inputValidator.validateStatus(status);

    SearchQuery query = new SearchQuery(status, limit, 0);
    SearchResult result = searchService.search(query);

    Map<String, Object> response = new HashMap<>();
    response.put("dataCenters", result.getResults());
    response.put("total", result.getResults().size());
    return ResponseEntity.ok(response);
  }

  /**
   * Filters data centers by region.
   *
   * @param region the region name
   * @param limit the maximum number of results
   * @return filtered list of data centers
   */
  @GetMapping("/filter/region")
  public ResponseEntity<Map<String, Object>> filterByRegion(
      @RequestParam(name = "region") String region,
      @RequestParam(name = "limit", defaultValue = "100") int limit) {
    // Validate parameters - limit must be 1-100 for SearchQuery
    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException("Limit must be between 1 and 100");
    }
    inputValidator.validateRegion(region);

    SearchQuery query = new SearchQuery(region, limit, 0);
    SearchResult result = searchService.search(query);

    Map<String, Object> response = new HashMap<>();
    response.put("dataCenters", result.getResults());
    response.put("total", result.getResults().size());
    return ResponseEntity.ok(response);
  }
}