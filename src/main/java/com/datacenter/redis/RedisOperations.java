package com.datacenter.redis;

import com.datacenter.model.DataCenter;
import java.util.List;

/**
 * Interface for Redis operations on data centers.
 * Provides methods for CRUD operations and filtering.
 */
public interface RedisOperations {

  /**
   * Saves a data center to Redis.
   *
   * @param dataCenter the data center to save
   */
  void saveDataCenter(DataCenter dataCenter);

  /**
   * Retrieves a data center by ID from Redis.
   *
   * @param id the data center ID (UUID)
   * @return the data center if found, null otherwise
   */
  DataCenter getDataCenter(String id);

  /**
   * Retrieves all data centers from Redis.
   *
   * @return list of all data centers
   */
  List<DataCenter> getAllDataCenters();

  /**
   * Deletes a data center from Redis.
   *
   * @param id the data center ID (UUID)
   */
  void deleteDataCenter(String id);

  /**
   * Filters data centers by operator.
   *
   * @param operator the operator name
   * @return list of data centers operated by the specified operator
   */
  List<DataCenter> filterByOperator(String operator);

  /**
   * Filters data centers by status.
   *
   * @param status the status value
   * @return list of data centers with the specified status
   */
  List<DataCenter> filterByStatus(String status);

  /**
   * Filters data centers by region.
   *
   * @param region the region name
   * @return list of data centers in the specified region
   */
  List<DataCenter> filterByRegion(String region);

  /**
   * Clears all data centers from Redis.
   */
  void clearAll();

  /**
   * Gets a value from Redis by key with type casting.
   *
   * @param key the Redis key
   * @param type the class type to deserialize to
   * @param <T> the generic type
   * @return the value if found, null otherwise
   */
  <T> T get(String key, Class<T> type);

  /**
   * Sets a value in Redis with TTL.
   *
   * @param key the Redis key
   * @param value the value to store
   * @param timeout the timeout duration
   * @param unit the timeout unit
   * @param <T> the generic type
   */
  <T> void set(String key, T value, long timeout, java.util.concurrent.TimeUnit unit);

  /**
   * Deletes a key from Redis.
   *
   * @param key the Redis key
   */
  void delete(String key);

  /**
   * Deletes all keys matching a pattern.
   *
   * @param pattern the key pattern (e.g., "search:*")
   */
  void deleteByPattern(String pattern);

  /**
   * Checks if a key exists in Redis.
   *
   * @param key the Redis key
   * @return true if the key exists, false otherwise
   */
  boolean exists(String key);

  /**
   * Gets a string value from Redis by key.
   *
   * @param key the Redis key
   * @return the value if found, null otherwise
   */
  String get(String key);

  /**
   * Sets a string value in Redis with TTL.
   *
   * @param key the Redis key
   * @param value the value to store
   * @param ttlSeconds the time-to-live in seconds
   */
  void setWithExpiry(String key, String value, long ttlSeconds);
}