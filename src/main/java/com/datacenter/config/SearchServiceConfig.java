package com.datacenter.config;

import com.datacenter.model.DataCenter;
import com.datacenter.redis.SearchCacheManager;
import com.datacenter.search.SearchAlgorithm;
import com.datacenter.search.SearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * Configuration for search service dependencies.
 * Provides beans for SearchService initialization with required dependencies.
 */
@Configuration
public class SearchServiceConfig {

  /**
   * Creates a SearchService bean with all required dependencies.
   *
   * @param searchAlgorithm the search algorithm implementation
   * @param redisTemplate the Redis template for caching
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