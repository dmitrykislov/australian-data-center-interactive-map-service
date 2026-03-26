package com.datacenter.search;

import com.datacenter.model.DataCenter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Implementation of SearchAlgorithm for case-insensitive partial matching.
 * Provides search functionality for data center names and operators.
 */
@Component
public class SearchAlgorithmImpl implements SearchAlgorithm {

  private static final double EXACT_MATCH_SCORE = 100.0;
  private static final double PREFIX_MATCH_SCORE = 80.0;
  private static final double SUBSTRING_MATCH_SCORE = 60.0;

  /**
   * Performs case-insensitive partial matching on a list of data centers.
   *
   * @param dataCenters the list of data centers to search
   * @param query the search query
   * @return list of matching data centers sorted by relevance
   */
  public List<DataCenter> search(List<DataCenter> dataCenters, String query) {
    if (dataCenters == null || dataCenters.isEmpty()) {
      return new ArrayList<>();
    }

    if (query == null || query.trim().isEmpty()) {
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
    double nameScore = scoreField(dataCenter.getName(), normalizedQuery);
    double operatorScore = scoreField(dataCenter.getOperator(), normalizedQuery);

    return Math.max(nameScore, operatorScore);
  }

  private double scoreField(String field, String normalizedQuery) {
    if (field == null) {
      return 0.0;
    }

    String normalizedField = normalizeString(field);

    if (normalizedField.equals(normalizedQuery)) {
      return EXACT_MATCH_SCORE;
    }

    if (normalizedField.startsWith(normalizedQuery)) {
      return PREFIX_MATCH_SCORE;
    }

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