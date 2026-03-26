# Redis Key Naming Convention

This document defines the Redis key naming conventions used in the DataCenter Mapping System.

## Overview

Redis keys are organized hierarchically using colons (`:`) as separators. All keys use lowercase letters, hyphens for multi-word identifiers, and UUIDs for entity IDs without transformation.

## Key Patterns

### Entity Keys

#### `datacenter:{id}`
- **Type**: Hash
- **Description**: Stores a complete DataCenter entity
- **Fields**: id, name, operator, coordinates (latitude, longitude), capacity, status, description (optional), tags (optional)
- **Example**: `datacenter:550e8400-e29b-41d4-a716-446655440000`
- **TTL**: Configurable via `datacenter:cache:ttl`
- **Usage**: Primary storage for individual data center records

### Collection Keys

#### `datacenter:all`
- **Type**: Set
- **Description**: Contains all DataCenter IDs for quick enumeration
- **Members**: UUID strings
- **Example**: `datacenter:all` → {uuid1, uuid2, uuid3, ...}
- **Usage**: Retrieve all data center IDs without scanning

### Index Keys

#### `datacenter:index:name`
- **Type**: Hash
- **Description**: Maps data center names to their IDs
- **Fields**: name → id
- **Example**: `datacenter:index:name` → {"NYC Data Center": "550e8400-e29b-41d4-a716-446655440000"}
- **Usage**: Fast lookup by name

#### `datacenter:index:operator`
- **Type**: Hash
- **Description**: Maps operator names to sets of data center IDs
- **Fields**: operator → comma-separated IDs or set
- **Example**: `datacenter:index:operator` → {"TechCorp": "id1,id2,id3"}
- **Usage**: Find all data centers operated by a specific operator

#### `datacenter:index:status`
- **Type**: Hash
- **Description**: Maps status values to sets of data center IDs
- **Fields**: status → comma-separated IDs or set
- **Example**: `datacenter:index:status` → {"operational": "id1,id2", "maintenance": "id3"}
- **Usage**: Find all data centers with a specific status

### Geospatial Index

#### `datacenter:geo`
- **Type**: Sorted Set (with geospatial index)
- **Description**: Geospatial index for location-based queries
- **Members**: Data center IDs with longitude/latitude scores
- **Usage**: Perform radius searches, find nearest data centers
- **Commands**: GEOADD, GEORADIUS, GEORADIUSBYMEMBER

### Stream Keys

#### `datacenter:updates:{id}`
- **Type**: Stream
- **Description**: Update stream for a specific data center
- **Entries**: Timestamped update events
- **Example**: `datacenter:updates:550e8400-e29b-41d4-a716-446655440000`
- **Usage**: Track changes to a specific data center in real-time

### Configuration Keys

#### `datacenter:cache:ttl`
- **Type**: String
- **Description**: Cache TTL configuration in seconds
- **Example**: `datacenter:cache:ttl` → "3600"
- **Default**: 3600 seconds (1 hour)
- **Usage**: Control how long data center records are cached

## Naming Conventions

### Rules
- **Lowercase**: All keys use lowercase letters
- **Separators**: Use colons (`:`) to create hierarchical structure
- **Multi-word identifiers**: Use hyphens (`-`) for multi-word parts
- **Entity IDs**: Use UUIDs without transformation or normalization
- **No spaces**: Avoid spaces and special characters except colons and hyphens

### Examples
✅ Valid:
- `datacenter:550e8400-e29b-41d4-a716-446655440000`
- `datacenter:index:name`
- `datacenter:updates:550e8400-e29b-41d4-a716-446655440000`
- `datacenter:cache:ttl`

❌ Invalid:
- `DataCenter:550e8400-e29b-41d4-a716-446655440000` (uppercase)
- `datacenter_all` (underscore instead of colon)
- `datacenter:NYC Data Center` (spaces in key)
- `datacenter:550e8400_e29b_41d4_a716_446655440000` (underscores in UUID)

## Implementation Guidelines

### Creating Keys
Use the `RedisKeyNamingConvention` utility class to generate keys:

```java
// Entity key
String key = RedisKeyNamingConvention.dataCenterKey(dataCenterId);

// Index keys
String nameIndexKey = RedisKeyNamingConvention.nameIndexKey();
String operatorIndexKey = RedisKeyNamingConvention.operatorIndexKey();
String statusIndexKey = RedisKeyNamingConvention.statusIndexKey();

// Geospatial index
String geoKey = RedisKeyNamingConvention.geoIndexKey();

// Update stream
String updateKey = RedisKeyNamingConvention.updateStreamKey(dataCenterId);

// Cache TTL
String ttlKey = RedisKeyNamingConvention.cacheTtlKey();
```

### Key Expiration
- Entity keys (`datacenter:{id}`): Use TTL from `datacenter:cache:ttl`
- Index keys: No expiration (manually invalidated on updates)
- Stream keys: No expiration (append-only)
- Configuration keys: No expiration

### Consistency
- Always use the `RedisKeyNamingConvention` class to generate keys
- Never hardcode key strings in application code
- Update the convention class if new key patterns are needed
- Document any new patterns in this file

## Performance Considerations

### Indexing Strategy
- **Name index**: O(1) lookup by name
- **Operator index**: O(1) lookup by operator, returns set of IDs
- **Status index**: O(1) lookup by status, returns set of IDs
- **Geospatial index**: O(log N) for radius searches

### Batch Operations
For bulk operations, use Redis pipelines to reduce round-trips:
- Update multiple indices in a single pipeline
- Maintain consistency across indices

### Cache Invalidation
When a data center is updated:
1. Update `datacenter:{id}` hash
2. Update all relevant indices (name, operator, status)
3. Update geospatial index if coordinates changed
4. Publish update to `datacenter:updates:{id}` stream

## Monitoring and Debugging

### Key Inspection
```bash
# List all data center IDs
SMEMBERS datacenter:all

# Get a specific data center
HGETALL datacenter:550e8400-e29b-41d4-a716-446655440000

# Find data centers by operator
HGET datacenter:index:operator "TechCorp"

# Get recent updates for a data center
XRANGE datacenter:updates:550e8400-e29b-41d4-a716-446655440000 - +
```

### Key Statistics
```bash
# Count all data centers
SCARD datacenter:all

# Check index sizes
HLEN datacenter:index:name
HLEN datacenter:index:operator
HLEN datacenter:index:status

# Check geospatial index size
ZCARD datacenter:geo
```