package com.datacenter.redis;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Redis Key Naming Convention Tests")
class RedisKeyNamingConventionTest {

  @Test
  @DisplayName("should generate correct datacenter key")
  void testDataCenterKey() {
    String id = UUID.randomUUID().toString();
    String key = RedisKeyNamingConvention.dataCenterKey(id);
    assertEquals("datacenter:" + id, key);
  }

  @Test
  @DisplayName("should reject null id for datacenter key")
  void testDataCenterKeyRejectNullId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> RedisKeyNamingConvention.dataCenterKey(null));
  }

  @Test
  @DisplayName("should reject empty id for datacenter key")
  void testDataCenterKeyRejectEmptyId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> RedisKeyNamingConvention.dataCenterKey(""));
  }

  @Test
  @DisplayName("should generate correct all datacenters key")
  void testAllDataCentersKey() {
    String key = RedisKeyNamingConvention.allDataCentersKey();
    assertEquals("datacenter:all", key);
  }

  @Test
  @DisplayName("should generate correct name index key")
  void testNameIndexKey() {
    String key = RedisKeyNamingConvention.nameIndexKey();
    assertEquals("datacenter:index:name", key);
  }

  @Test
  @DisplayName("should generate correct operator index key")
  void testOperatorIndexKey() {
    String key = RedisKeyNamingConvention.operatorIndexKey();
    assertEquals("datacenter:index:operator", key);
  }

  @Test
  @DisplayName("should generate correct status index key")
  void testStatusIndexKey() {
    String key = RedisKeyNamingConvention.statusIndexKey();
    assertEquals("datacenter:index:status", key);
  }

  @Test
  @DisplayName("should generate correct geo index key")
  void testGeoIndexKey() {
    String key = RedisKeyNamingConvention.geoIndexKey();
    assertEquals("datacenter:geo", key);
  }

  @Test
  @DisplayName("should generate correct update stream key")
  void testUpdateStreamKey() {
    String id = UUID.randomUUID().toString();
    String key = RedisKeyNamingConvention.updateStreamKey(id);
    assertEquals("datacenter:updates:" + id, key);
  }

  @Test
  @DisplayName("should reject null id for update stream key")
  void testUpdateStreamKeyRejectNullId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> RedisKeyNamingConvention.updateStreamKey(null));
  }

  @Test
  @DisplayName("should reject empty id for update stream key")
  void testUpdateStreamKeyRejectEmptyId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> RedisKeyNamingConvention.updateStreamKey(""));
  }

  @Test
  @DisplayName("should generate correct cache ttl key")
  void testCacheTtlKey() {
    String key = RedisKeyNamingConvention.cacheTtlKey();
    assertEquals("datacenter:cache:ttl", key);
  }

  @Test
  @DisplayName("should use consistent separator")
  void testConsistentSeparator() {
    String id = UUID.randomUUID().toString();
    String dcKey = RedisKeyNamingConvention.dataCenterKey(id);
    String allKey = RedisKeyNamingConvention.allDataCentersKey();
    String nameKey = RedisKeyNamingConvention.nameIndexKey();

    assertTrue(dcKey.contains(":"));
    assertTrue(allKey.contains(":"));
    assertTrue(nameKey.contains(":"));
  }

  @Test
  @DisplayName("should use lowercase prefix")
  void testLowercasePrefix() {
    String allKey = RedisKeyNamingConvention.allDataCentersKey();
    assertTrue(allKey.startsWith("datacenter:"));
    assertEquals(allKey, allKey.toLowerCase());
  }

  @Test
  @DisplayName("should generate unique keys for different operations")
  void testUniqueKeysForDifferentOperations() {
    String id = UUID.randomUUID().toString();
    String dcKey = RedisKeyNamingConvention.dataCenterKey(id);
    String updateKey = RedisKeyNamingConvention.updateStreamKey(id);
    String allKey = RedisKeyNamingConvention.allDataCentersKey();

    assertNotEquals(dcKey, updateKey);
    assertNotEquals(dcKey, allKey);
    assertNotEquals(updateKey, allKey);
  }

  @Test
  @DisplayName("should handle UUID format correctly")
  void testHandleUuidFormat() {
    String uuid = "550e8400-e29b-41d4-a716-446655440000";
    String key = RedisKeyNamingConvention.dataCenterKey(uuid);
    assertEquals("datacenter:" + uuid, key);
  }
}