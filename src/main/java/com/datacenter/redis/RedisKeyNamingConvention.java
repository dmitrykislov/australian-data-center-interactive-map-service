package com.datacenter.redis;

/**
 * Defines Redis key naming conventions for data center entities and related data.
 *
 * <p>Key Naming Patterns:
 * <ul>
 *   <li>{@code datacenter:{id}} - Individual data center entity (hash)
 *   <li>{@code datacenter:all} - Set of all data center IDs
 *   <li>{@code datacenter:index:name} - Index mapping names to IDs (hash)
 *   <li>{@code datacenter:index:operator} - Index mapping operators to IDs (hash)
 *   <li>{@code datacenter:index:status} - Index mapping statuses to ID sets (hash)
 *   <li>{@code datacenter:geo} - Geospatial index for location-based queries (sorted set)
 *   <li>{@code datacenter:updates:{id}} - Update stream for a specific data center
 *   <li>{@code datacenter:cache:ttl} - Cache TTL configuration (string)
 * </ul>
 *
 * <p>Naming Conventions:
 * <ul>
 *   <li>Use lowercase for all keys
 *   <li>Use colons (:) as separators for hierarchical structure
 *   <li>Use hyphens (-) for multi-word identifiers
 *   <li>Use UUIDs for entity IDs (no transformation)
 *   <li>Avoid spaces and special characters except colons and hyphens
 * </ul>
 */
public final class RedisKeyNamingConvention {

  private static final String PREFIX = "datacenter";
  private static final String SEPARATOR = ":";

  private RedisKeyNamingConvention() {
    // Utility class, no instantiation
  }

  /**
   * Generates the Redis key for a specific data center entity.
   *
   * @param dataCenterId the UUID of the data center
   * @return the Redis key for the data center hash
   */
  public static String dataCenterKey(String dataCenterId) {
    validateId(dataCenterId);
    return PREFIX + SEPARATOR + dataCenterId;
  }

  /**
   * Gets the Redis key for the set of all data center IDs.
   *
   * @return the Redis key for the all data centers set
   */
  public static String allDataCentersKey() {
    return PREFIX + SEPARATOR + "all";
  }

  /**
   * Gets the Redis key for the name-to-ID index.
   *
   * @return the Redis key for the name index hash
   */
  public static String nameIndexKey() {
    return PREFIX + SEPARATOR + "index" + SEPARATOR + "name";
  }

  /**
   * Gets the Redis key for the operator-to-ID index.
   *
   * @return the Redis key for the operator index hash
   */
  public static String operatorIndexKey() {
    return PREFIX + SEPARATOR + "index" + SEPARATOR + "operator";
  }

  /**
   * Gets the Redis key for the status-to-IDs index.
   *
   * @return the Redis key for the status index hash
   */
  public static String statusIndexKey() {
    return PREFIX + SEPARATOR + "index" + SEPARATOR + "status";
  }

  /**
   * Gets the Redis key for the geospatial index.
   *
   * @return the Redis key for the geospatial sorted set
   */
  public static String geoIndexKey() {
    return PREFIX + SEPARATOR + "geo";
  }

  /**
   * Gets the Redis key for the update stream of a specific data center.
   *
   * @param dataCenterId the UUID of the data center
   * @return the Redis key for the update stream
   */
  public static String updateStreamKey(String dataCenterId) {
    validateId(dataCenterId);
    return PREFIX + SEPARATOR + "updates" + SEPARATOR + dataCenterId;
  }

  /**
   * Gets the Redis key for cache TTL configuration.
   *
   * @return the Redis key for the cache TTL setting
   */
  public static String cacheTtlKey() {
    return PREFIX + SEPARATOR + "cache" + SEPARATOR + "ttl";
  }

  private static void validateId(String id) {
    if (id == null || id.trim().isEmpty()) {
      throw new IllegalArgumentException("Data center ID cannot be null or empty");
    }
  }
}