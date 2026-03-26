# API Reference

## Base URL

```
https://localhost:8443/api/v1
```

All requests must use HTTPS. HTTP requests are automatically redirected to HTTPS.

## Authentication

This API does not require authentication. All endpoints are publicly accessible.

## Data Centers Endpoints

### Get All Data Centers

```
GET /datacenters
```

Returns a list of all data centers.

**Query Parameters:**
- `limit` (optional): Maximum number of results (default: 100)
- `offset` (optional): Number of results to skip (default: 0)

**Response:**
```json
{
  "results": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Sydney Data Center",
      "operator": "Equinix",
      "coordinates": {
        "latitude": -33.8688,
        "longitude": 151.2093
      },
      "capacity": 1000,
      "status": "OPERATIONAL",
      "description": "Sydney facility"
    }
  ],
  "totalCount": 20,
  "limit": 100,
  "offset": 0
}
```

**Status Codes:**
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters
- `500 Internal Server Error`: Server error

### Get Data Center by ID

```
GET /datacenters/{id}
```

Returns a specific data center by ID.

**Path Parameters:**
- `id` (required): Data center UUID

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Sydney Data Center",
  "operator": "Equinix",
  "coordinates": {
    "latitude": -33.8688,
    "longitude": 151.2093
  },
  "capacity": 1000,
  "status": "OPERATIONAL",
  "description": "Sydney facility"
}
```

**Status Codes:**
- `200 OK`: Success
- `400 Bad Request`: Invalid UUID
- `404 Not Found`: Data center not found
- `500 Internal Server Error`: Server error

### Filter Data Centers

```
GET /datacenters/filter
```

Filter data centers by operator, region, or status.

**Query Parameters:**
- `operator` (optional): Filter by operator name
- `region` (optional): Filter by region
- `status` (optional): Filter by status (OPERATIONAL, PLANNED)
- `limit` (optional): Maximum number of results
- `offset` (optional): Number of results to skip

**Response:**
```json
{
  "results": [...],
  "totalCount": 5,
  "limit": 100,
  "offset": 0
}
```

**Status Codes:**
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters
- `500 Internal Server Error`: Server error

## Search Endpoints

### Search Data Centers

```
GET /search
```

Search for data centers by name or operator.

**Query Parameters:**
- `q` (required): Search query
- `limit` (optional): Maximum number of results (default: 10)

**Response:**
```json
{
  "results": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Sydney Data Center",
      "operator": "Equinix",
      "coordinates": {...},
      "capacity": 1000,
      "status": "OPERATIONAL"
    }
  ],
  "totalCount": 1,
  "limit": 10,
  "offset": 0,
  "executionTimeMs": 45
}
```

**Status Codes:**
- `200 OK`: Success
- `400 Bad Request`: Invalid query
- `500 Internal Server Error`: Server error

### Autocomplete

```
GET /search/autocomplete
```

Get autocomplete suggestions for data center names.

**Query Parameters:**
- `q` (required): Partial search query
- `limit` (optional): Maximum number of suggestions (default: 10)

**Response:**
```json
{
  "suggestions": [
    "Sydney Data Center",
    "Sydney Metro",
    "Sydney East"
  ]
}
```

**Status Codes:**
- `200 OK`: Success
- `400 Bad Request`: Invalid query
- `500 Internal Server Error`: Server error

## Analytics Endpoints

### Track Page View

```
POST /analytics/page-view
```

Track an anonymous page view.

**Request Body:**
```json
{
  "pagePath": "/",
  "referrer": "https://example.com",
  "userAgentType": "desktop"
}
```

**Parameters:**
- `pagePath` (required): Page path (e.g., "/", "/map", "/search")
- `referrer` (optional): HTTP referrer
- `userAgentType` (optional): User agent type (desktop, mobile, tablet, unknown)

**Response:**
```json
{
  "success": true
}
```

**Status Codes:**
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters
- `500 Internal Server Error`: Server error

### Track API Call

```
POST /analytics/api-call
```

Track an anonymous API call.

**Request Body:**
```json
{
  "endpoint": "/api/v1/datacenters",
  "statusCode": 200,
  "responseTimeMs": 150
}
```

**Parameters:**
- `endpoint` (required): API endpoint
- `statusCode` (required): HTTP status code
- `responseTimeMs` (optional): Response time in milliseconds

**Response:**
```json
{
  "success": true
}
```

**Status Codes:**
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters
- `500 Internal Server Error`: Server error

### Track Error

```
POST /analytics/error
```

Track an error event.

**Request Body:**
```json
{
  "endpoint": "/api/v1/datacenters",
  "statusCode": 500,
  "errorMessage": "Internal Server Error"
}
```

**Parameters:**
- `endpoint` (required): API endpoint where error occurred
- `statusCode` (required): HTTP status code
- `errorMessage` (optional): Error message

**Response:**
```json
{
  "success": true
}
```

**Status Codes:**
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters
- `500 Internal Server Error`: Server error

## Attribution Endpoints

### Get Attribution Providers

```
GET /attribution/providers
```

Get all attribution providers (tile providers and data sources).

**Query Parameters:**
- `type` (optional): Filter by type (tile_provider, data_source)

**Response:**
```json
{
  "providers": [
    {
      "name": "OpenStreetMap",
      "type": "tile_provider",
      "url": "https://www.openstreetmap.org",
      "attribution": "© OpenStreetMap contributors"
    },
    {
      "name": "Australian Data Centers",
      "type": "data_source",
      "url": "https://example.com/data",
      "attribution": "Australian Data Center Information"
    }
  ]
}
```

**Status Codes:**
- `200 OK`: Success
- `400 Bad Request`: Invalid parameters
- `500 Internal Server Error`: Server error

## Error Responses

All error responses follow this format:

```json
{
  "error": "Error message",
  "status": 400,
  "timestamp": "2024-03-27T10:30:00Z"
}
```

### Common Error Codes

- `400 Bad Request`: Invalid request parameters
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error
- `503 Service Unavailable`: Service temporarily unavailable

## Rate Limiting

Currently, there is no rate limiting on the API. However, rate limiting may be implemented in the future.

## Caching

### Cache Headers

Responses include cache headers:
- `Cache-Control: public, max-age=300` - Cache for 5 minutes
- `ETag: "hash"` - Entity tag for cache validation

### Conditional Requests

Use `If-None-Match` header with ETag value to check if resource has changed:

```
GET /datacenters/550e8400-e29b-41d4-a716-446655440000
If-None-Match: "abc123"
```

Response:
- `304 Not Modified` - Resource unchanged
- `200 OK` - Resource changed, returns new data

## CORS

CORS is configured to allow same-origin requests only:

**Allowed Origins:**
- `http://localhost:8080`
- `https://localhost:8080`

**Allowed Methods:**
- GET, POST, PUT, DELETE, OPTIONS

**Allowed Headers:**
- All headers

## Security

All endpoints require HTTPS. HTTP requests are automatically redirected to HTTPS.

### Security Headers

All responses include security headers:
- `Strict-Transport-Security`: Forces HTTPS
- `Content-Security-Policy`: Restricts resource loading
- `X-Frame-Options`: Prevents clickjacking
- `X-Content-Type-Options`: Prevents MIME sniffing
- `X-XSS-Protection`: Enables XSS filter

### Input Validation

All input parameters are validated:
- Path parameters must be valid UUIDs
- Query parameters must match expected patterns
- Request bodies must be valid JSON

Invalid input returns `400 Bad Request` with error details.

## Versioning

The API uses URL versioning: `/api/v1/`

Future versions will be available at `/api/v2/`, etc.

## Changelog

### Version 1.0.0
- Initial release
- Data center endpoints
- Search endpoints
- Analytics endpoints
- Attribution endpoints