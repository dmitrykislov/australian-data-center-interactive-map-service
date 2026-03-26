package com.datacenter.config;

import com.datacenter.australia.AustralianDataCenterLoader;
import com.datacenter.model.DataCenter;
import com.datacenter.redis.SearchCacheManager;
import com.datacenter.search.SearchAlgorithm;
import com.datacenter.search.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for search service dependencies.
 * Provides beans for SearchService initialization with required dependencies.
 */
@Configuration
public class SearchServiceConfig {

  private static final Logger logger = LoggerFactory.getLogger(SearchServiceConfig.class);

  /**
   * Loads all data centers from the classpath JSON file at application startup.
   *
   * @return immutable list of all data centers
   */
  @Bean
  public List<DataCenter> dataCenters() {
    AustralianDataCenterLoader loader = new AustralianDataCenterLoader();
    try {
      List<DataCenter> loaded = loader.loadFromClasspath();
      logger.info("Loaded {} data centers from classpath", loaded.size());
      return Collections.unmodifiableList(loaded);
    } catch (IOException e) {
      logger.error("Failed to load data centers from classpath, starting with empty list", e);
      return Collections.emptyList();
    }
  }

  /**
   * Creates a SearchService bean with all required dependencies.
   *
   * @param searchAlgorithm the search algorithm implementation
   * @param cacheManager the cache manager for storing results
   * @param dataCenters the list of data centers to search
   * @return configured SearchService instance
   */
  @Bean
  public SearchService searchService(
      SearchAlgorithm searchAlgorithm,
      SearchCacheManager cacheManager,
      List<DataCenter> dataCenters) {
    return new SearchService(searchAlgorithm, cacheManager, dataCenters);
  }
}