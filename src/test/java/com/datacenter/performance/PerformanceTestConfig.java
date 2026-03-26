package com.datacenter.performance;

/**
 * Configuration constants for performance and load testing.
 * Defines SLA targets and load test parameters.
 */
public final class PerformanceTestConfig {

  // SLA targets
  public static final long P99_RESPONSE_TIME_MS = 200;
  public static final long P95_RESPONSE_TIME_MS = 150;
  public static final long P50_RESPONSE_TIME_MS = 50;

  // Load test parameters
  public static final int CONCURRENT_USERS = 1000;
  public static final int RAMP_UP_DURATION_SECONDS = 60;
  public static final int STEADY_STATE_DURATION_SECONDS = 300;
  public static final int RAMP_DOWN_DURATION_SECONDS = 30;

  // Workflow distribution (percentages)
  public static final int MAP_LOAD_PERCENTAGE = 30;
  public static final int FILTER_PERCENTAGE = 40;
  public static final int SEARCH_PERCENTAGE = 30;

  // Endpoints
  public static final String BASE_URL = "https://localhost:8443";
  public static final String GET_ALL_ENDPOINT = "/api/datacenters";
  public static final String FILTER_ENDPOINT = "/api/datacenters/filter";
  public static final String SEARCH_ENDPOINT = "/api/search";
  public static final String AUTOCOMPLETE_ENDPOINT = "/api/autocomplete";

  // Test data
  public static final String[] OPERATORS = {
    "Equinix",
    "Digital Realty",
    "NextDC",
    "AirtrunkLimited",
    "Canberra Data Centres"
  };

  public static final String[] REGIONS = {
    "Sydney",
    "Melbourne",
    "Brisbane",
    "Perth",
    "Canberra"
  };

  public static final String[] SEARCH_TERMS = {
    "Sydney",
    "Equinix",
    "operational",
    "NextDC",
    "Melbourne"
  };

  private PerformanceTestConfig() {
    // Utility class
  }
}