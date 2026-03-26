package com.datacenter.search;

import com.datacenter.model.DataCenter;
import java.util.List;

/**
 * Interface for search algorithm implementations.
 * Provides case-insensitive partial matching on facility names, operators, and cities.
 */
public interface SearchAlgorithm {

  /**
   * Searches a list of data centers for matches against the query.
   * Uses case-insensitive partial matching with relevance scoring.
   *
   * @param dataCenters the list of data centers to search
   * @param query the search query (case-insensitive)
   * @return list of matching data centers sorted by relevance
   */
  List<DataCenter> search(List<DataCenter> dataCenters, String query);

  /**
   * Gets autocomplete suggestions based on a partial query.
   * Returns facility names that match the query prefix.
   *
   * @param dataCenters the list of data centers to search
   * @param query the partial query string
   * @param limit maximum number of suggestions to return
   * @return list of matching facility names
   */
  List<String> getAutocompleteSuggestions(
      List<DataCenter> dataCenters, String query, int limit);
}