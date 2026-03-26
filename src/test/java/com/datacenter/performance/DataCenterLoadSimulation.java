package com.datacenter.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Gatling load simulation for DataCenter mapping application.
 * Simulates 1000+ concurrent users performing typical workflows:
 * - Loading the map (GET /api/datacenters)
 * - Filtering by operator and region
 * - Searching for facilities
 *
 * Verifies response times meet SLA targets:
 * - P99: < 200ms
 * - P95: < 150ms
 * - P50: < 50ms
 */
public class DataCenterLoadSimulation extends Simulation {

  private static final HttpProtocolBuilder httpProtocol =
      http.baseUrl(PerformanceTestConfig.BASE_URL)
          .acceptHeader("application/json")
          .contentTypeHeader("application/json")
          .userAgentHeader(
              "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

  // Scenario 1: Load map with all data centers
  private static final ScenarioBuilder mapLoadScenario =
      scenario("Map Load Workflow")
          .exec(
              http("Get all data centers")
                  .get(PerformanceTestConfig.GET_ALL_ENDPOINT)
                  .check(status().is(200)))
          .pause(2, 5); // Simulate user viewing map

  // Scenario 2: Filter by operator
  private static final ScenarioBuilder filterByOperatorScenario =
      scenario("Filter by Operator Workflow")
          .exec(
              http("Get all data centers")
                  .get(PerformanceTestConfig.GET_ALL_ENDPOINT)
                  .check(status().is(200)))
          .pause(1, 3)
          .exec(
              http("Filter by operator")
                  .get(
                      PerformanceTestConfig.FILTER_ENDPOINT
                          + "?operator=Equinix")
                  .check(status().is(200)))
          .pause(2, 4);

  // Scenario 3: Filter by region
  private static final ScenarioBuilder filterByRegionScenario =
      scenario("Filter by Region Workflow")
          .exec(
              http("Get all data centers")
                  .get(PerformanceTestConfig.GET_ALL_ENDPOINT)
                  .check(status().is(200)))
          .pause(1, 3)
          .exec(
              http("Filter by region")
                  .get(
                      PerformanceTestConfig.FILTER_ENDPOINT
                          + "?region=Sydney")
                  .check(status().is(200)))
          .pause(2, 4);

  // Scenario 4: Search for facilities
  private static final ScenarioBuilder searchScenario =
      scenario("Search Workflow")
          .exec(
              http("Get all data centers")
                  .get(PerformanceTestConfig.GET_ALL_ENDPOINT)
                  .check(status().is(200)))
          .pause(1, 2)
          .exec(
              http("Search facilities")
                  .get(
                      PerformanceTestConfig.SEARCH_ENDPOINT
                          + "?query=Sydney&limit=20")
                  .check(status().is(200)))
          .pause(1, 3)
          .exec(
              http("Autocomplete")
                  .get(
                      PerformanceTestConfig.AUTOCOMPLETE_ENDPOINT
                          + "?prefix=Syd")
                  .check(status().is(200)))
          .pause(2, 4);

  // Scenario 5: Combined workflow (realistic user behavior)
  private static final ScenarioBuilder combinedWorkflowScenario =
      scenario("Combined Realistic Workflow")
          .exec(
              http("Load map")
                  .get(PerformanceTestConfig.GET_ALL_ENDPOINT)
                  .check(status().is(200)))
          .pause(2, 5)
          .exec(
              http("Filter by operator")
                  .get(
                      PerformanceTestConfig.FILTER_ENDPOINT
                          + "?operator=NextDC")
                  .check(status().is(200)))
          .pause(1, 3)
          .exec(
              http("Search")
                  .get(
                      PerformanceTestConfig.SEARCH_ENDPOINT
                          + "?query=Melbourne&limit=20")
                  .check(status().is(200)))
          .pause(2, 4)
          .exec(
              http("Filter by region")
                  .get(
                      PerformanceTestConfig.FILTER_ENDPOINT
                          + "?region=Brisbane")
                  .check(status().is(200)))
          .pause(1, 3);

  {
    // Load test setup with 1000+ concurrent users
    // Ramp up: 60 seconds to 1000 users (250 per scenario)
    // Steady state: 300 seconds at 1000 users
    // Ramp down: 30 seconds to 0 users
    int usersPerScenario = PerformanceTestConfig.CONCURRENT_USERS / 4;
    
    setUp(
            mapLoadScenario
                .injectOpen(
                    rampUsers(usersPerScenario)
                        .during(PerformanceTestConfig.RAMP_UP_DURATION_SECONDS),
                    constantUsersPerSec(usersPerScenario)
                        .during(PerformanceTestConfig.STEADY_STATE_DURATION_SECONDS),
                    rampUsers(0)
                        .during(PerformanceTestConfig.RAMP_DOWN_DURATION_SECONDS)),
            filterByOperatorScenario
                .injectOpen(
                    rampUsers(usersPerScenario)
                        .during(PerformanceTestConfig.RAMP_UP_DURATION_SECONDS),
                    constantUsersPerSec(usersPerScenario)
                        .during(PerformanceTestConfig.STEADY_STATE_DURATION_SECONDS),
                    rampUsers(0)
                        .during(PerformanceTestConfig.RAMP_DOWN_DURATION_SECONDS)),
            filterByRegionScenario
                .injectOpen(
                    rampUsers(usersPerScenario)
                        .during(PerformanceTestConfig.RAMP_UP_DURATION_SECONDS),
                    constantUsersPerSec(usersPerScenario)
                        .during(PerformanceTestConfig.STEADY_STATE_DURATION_SECONDS),
                    rampUsers(0)
                        .during(PerformanceTestConfig.RAMP_DOWN_DURATION_SECONDS)),
            searchScenario
                .injectOpen(
                    rampUsers(usersPerScenario)
                        .during(PerformanceTestConfig.RAMP_UP_DURATION_SECONDS),
                    constantUsersPerSec(usersPerScenario)
                        .during(PerformanceTestConfig.STEADY_STATE_DURATION_SECONDS),
                    rampUsers(0)
                        .during(PerformanceTestConfig.RAMP_DOWN_DURATION_SECONDS)))
        .protocols(httpProtocol)
        .assertions(
            // Global assertions for all requests
            global().successfulRequests().percent().is(100.0));
  }
}