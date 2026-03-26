package com.datacenter.redis;

/**
 * Interface for Redis operations used by the search cache manager.
 * Abstracts Redis interactions for easier testing and flexibility.
 */
public interface RedisOperations {

  /**
   * Gets a value from Redis.
   *
   * @param key the Redis key
   * @return the value, or null if not found
   */
  String get(String key);

  /**
   * Sets a value in Redis with expiration.
   *
   * @param key the Redis key
   * @param value the value to set
   * @param ttlSeconds the time-to-live in seconds
   */
  void setWithExpiry(String key, String value, long ttlSeconds);

  /**
   * Deletes keys matching a pattern.
   *
   * @param pattern the pattern (supports * wildcard)
   */
  void deleteByPattern(String pattern);
}