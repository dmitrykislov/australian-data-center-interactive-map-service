package com.datacenter.search;

import com.datacenter.model.DataCenter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of a search query.
 * Contains matching data centers and metadata about the search.
 */
public class SearchResult {

  private final List<DataCenter> results;
  private final int totalCount;
  private final int limit;
  private final int offset;
  private final long executionTimeMs;

  /**
   * Creates a search result.
   *
   * @param results list of matching data centers
   * @param totalCount total number of matches (before pagination)
   * @param limit pagination limit
   * @param offset pagination offset
   * @param executionTimeMs time taken to execute the search in milliseconds
   */
  public SearchResult(
      List<DataCenter> results, int totalCount, int limit, int offset, long executionTimeMs) {
    this.results = Collections.unmodifiableList(Objects.requireNonNull(results));
    this.totalCount = validateTotalCount(totalCount);
    this.limit = limit;
    this.offset = offset;
    this.executionTimeMs = validateExecutionTime(executionTimeMs);
  }

  private static int validateTotalCount(int totalCount) {
    if (totalCount < 0) {
      throw new IllegalArgumentException(
          String.format("Total count must be non-negative, got %d", totalCount));
    }
    return totalCount;
  }

  private static long validateExecutionTime(long executionTimeMs) {
    if (executionTimeMs < 0) {
      throw new IllegalArgumentException(
          String.format("Execution time must be non-negative, got %d", executionTimeMs));
    }
    return executionTimeMs;
  }

  public List<DataCenter> getResults() {
    return results;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public int getLimit() {
    return limit;
  }

  public int getOffset() {
    return offset;
  }

  public long getExecutionTimeMs() {
    return executionTimeMs;
  }

  public int getResultCount() {
    return results.size();
  }

  public boolean hasMore() {
    return offset + results.size() < totalCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SearchResult that = (SearchResult) o;
    return totalCount == that.totalCount
        && limit == that.limit
        && offset == that.offset
        && executionTimeMs == that.executionTimeMs
        && Objects.equals(results, that.results);
  }

  @Override
  public int hashCode() {
    return Objects.hash(results, totalCount, limit, offset, executionTimeMs);
  }

  @Override
  public String toString() {
    return String.format(
        "SearchResult{resultCount=%d, totalCount=%d, limit=%d, offset=%d, executionTimeMs=%d}",
        getResultCount(), totalCount, limit, offset, executionTimeMs);
  }
}