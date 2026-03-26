package com.datacenter.search;

import java.util.Objects;

/**
 * Represents a search query for data centers.
 * Supports searching by facility name, operator, and city with optional pagination.
 */
public class SearchQuery {

  private final String query;
  private final int limit;
  private final int offset;

  /**
   * Creates a search query with default pagination (limit=20, offset=0).
   *
   * @param query the search query string (case-insensitive, supports partial matching)
   */
  public SearchQuery(String query) {
    this(query, 20, 0);
  }

  /**
   * Creates a search query with custom pagination.
   *
   * @param query the search query string
   * @param limit maximum number of results to return
   * @param offset number of results to skip
   */
  public SearchQuery(String query, int limit, int offset) {
    this.query = validateQuery(query);
    this.limit = validateLimit(limit);
    this.offset = validateOffset(offset);
  }

  private static String validateQuery(String query) {
    // Null queries are not allowed - must throw
    if (query == null) {
      throw new IllegalArgumentException("Search query cannot be null");
    }
    // Empty queries are allowed - treat as "match all"
    if (query.trim().isEmpty()) {
      return "";
    }
    if (query.length() > 255) {
      throw new IllegalArgumentException(
          String.format("Search query must not exceed 255 characters, got %d", query.length()));
    }
    return query.trim();
  }

  private static int validateLimit(int limit) {
    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException(
          String.format("Limit must be between 1 and 100, got %d", limit));
    }
    return limit;
  }

  private static int validateOffset(int offset) {
    if (offset < 0) {
      throw new IllegalArgumentException(
          String.format("Offset must be non-negative, got %d", offset));
    }
    return offset;
  }

  public String getQuery() {
    return query;
  }

  public int getLimit() {
    return limit;
  }

  public int getOffset() {
    return offset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SearchQuery that = (SearchQuery) o;
    return limit == that.limit && offset == that.offset && Objects.equals(query, that.query);
  }

  @Override
  public int hashCode() {
    return Objects.hash(query, limit, offset);
  }

  @Override
  public String toString() {
    return String.format("SearchQuery{query='%s', limit=%d, offset=%d}", query, limit, offset);
  }
}