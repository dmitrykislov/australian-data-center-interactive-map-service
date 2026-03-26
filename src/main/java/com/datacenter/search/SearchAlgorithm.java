package com.datacenter.search;

import com.datacenter.model.DataCenter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements the search algorithm for data centers.
 * Provides case-insensitive partial matching on facility names, operators, and cities.
 */
public class SearchAlgorithm {

  private static final double EXACT_MATCH_SCORE = 100.0;
  private static final double PREFIX_MATCH_SCORE = 80.0;
  private static final double SUBSTRING_MATCH_SCORE = 60.0;

  /**
   * Searches a list of data centers for matches against the query.
   * Uses case-insensitive partial matching with relevance scoring.
   *
   * @param dataCenters the list of data centers to search
   * @param query the search query (case-insensitive)
   * @return list of matching data centers sorted by relevance
   */
  public List<DataCenter> search(List<DataCenter> dataCenters, String query) {
    if (dataCenters == null || dataCenters.isEmpty()) {
      return new ArrayList<>();
    }

    String normalizedQuery = normalizeString(query);

    return dataCenters.stream()
        .map(dc -> new SearchMatch(dc, calculateRelevanceScore(dc, normalizedQuery)))
        .filter(match -> match.score > 0)
        .sorted(Comparator.comparingDouble(SearchMatch::getScore).reversed())
        .map(SearchMatch::getDataCenter)
        .collect(Collectors.toList());
  }

  /**
   * Gets autocomplete suggestions based on a partial query.
   * Returns facility names that match the query prefix.
   *
   * @param dataCenters the list of data centers to search
   * @param query the partial query string
   * @param limit maximum number of suggestions to return
   * @return list of matching facility names
   */
  public List<String> getAutocompleteSuggestions(
      List<DataCenter> dataCenters, String query, int limit) {
    if (dataCenters == null || dataCenters.isEmpty()) {
      return new ArrayList<>();
    }

    String normalizedQuery = normalizeString(query);

    return dataCenters.stream()
        .map(DataCenter::getName)
        .distinct()
        .filter(name -> normalizeString(name).startsWith(normalizedQuery))
        .sorted()
        .limit(limit)
        .collect(Collectors.toList());
  }

  private double calculateRelevanceScore(DataCenter dataCenter, String normalizedQuery) {
    double maxScore = 0.0;

    // Check facility name
    maxScore = Math.max(maxScore, scoreField(dataCenter.getName(), normalizedQuery));

    // Check operator
    maxScore = Math.max(maxScore, scoreField(dataCenter.getOperator(), normalizedQuery));

    // Check description if present
    if (dataCenter.getDescription() != null) {
      maxScore = Math.max(maxScore, scoreField(dataCenter.getDescription(), normalizedQuery));
    }

    // Check tags if present
    if (dataCenter.getTags() != null) {
      maxScore = Math.max(maxScore, scoreField(dataCenter.getTags(), normalizedQuery));
    }

    return maxScore;
  }

  private double scoreField(String field, String normalizedQuery) {
    if (field == null || field.isEmpty()) {
      return 0.0;
    }

    String normalizedField = normalizeString(field);

    // Exact match (case-insensitive)
    if (normalizedField.equals(normalizedQuery)) {
      return EXACT_MATCH_SCORE;
    }

    // Prefix match
    if (normalizedField.startsWith(normalizedQuery)) {
      return PREFIX_MATCH_SCORE;
    }

    // Substring match
    if (normalizedField.contains(normalizedQuery)) {
      return SUBSTRING_MATCH_SCORE;
    }

    return 0.0;
  }

  private String normalizeString(String str) {
    if (str == null) {
      return "";
    }
    return str.toLowerCase().trim();
  }

  /**
   * Internal class to pair a DataCenter with its relevance score.
   */
  private static class SearchMatch {
    private final DataCenter dataCenter;
    private final double score;

    SearchMatch(DataCenter dataCenter, double score) {
      this.dataCenter = dataCenter;
      this.score = score;
    }

    DataCenter getDataCenter() {
      return dataCenter;
    }

    double getScore() {
      return score;
    }
  }
}