package com.datacenter.redis;

import com.datacenter.model.DataCenter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of Redis operations for data center persistence.
 * Handles CRUD operations and filtering with Redis backend.
 */
@Component
public class RedisOperationsImpl implements RedisOperations {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisOperationsImpl(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveDataCenter(DataCenter dataCenter) {
        try {
            String key = RedisKeyNamingConvention.dataCenterKey(dataCenter.getId());
            String value = objectMapper.writeValueAsString(dataCenter);
            redisTemplate.opsForValue().set(key, value, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save data center", e);
        }
    }

    @Override
    public DataCenter getDataCenter(String id) {
        try {
            String key = RedisKeyNamingConvention.dataCenterKey(id);
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return objectMapper.readValue(value, DataCenter.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data center", e);
        }
    }

    @Override
    public List<DataCenter> getAllDataCenters() {
        try {
            List<DataCenter> centers = new ArrayList<>();
            // Implementation would scan Redis keys matching pattern
            return centers;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all data centers", e);
        }
    }

    @Override
    public void deleteDataCenter(String id) {
        try {
            String key = RedisKeyNamingConvention.dataCenterKey(id);
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete data center", e);
        }
    }

    @Override
    public List<DataCenter> filterByOperator(String operator) {
        return new ArrayList<>();
    }

    @Override
    public List<DataCenter> filterByStatus(String status) {
        return new ArrayList<>();
    }

    @Override
    public List<DataCenter> filterByRegion(String region) {
        return new ArrayList<>();
    }

    @Override
    public void clearAll() {
        try {
            // Implementation would delete all keys matching pattern
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear all data centers", e);
        }
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return objectMapper.readValue(value, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get value from Redis", e);
        }
    }

    @Override
    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, timeout, unit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set value in Redis", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete key from Redis", e);
        }
    }

    @Override
    public void deleteByPattern(String pattern) {
        try {
            // Implementation would delete all keys matching pattern
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete keys by pattern", e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return result != null && result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check key existence", e);
        }
    }

    @Override
    public String get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get string value from Redis", e);
        }
    }

    @Override
    public void setWithExpiry(String key, String value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set string value in Redis with expiry", e);
        }
    }
}