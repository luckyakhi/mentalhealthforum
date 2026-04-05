# API Design Guidelines

## REST Conventions

### URL Structure
```
/api/v{version}/{resource}
/api/v{version}/{resource}/{id}
/api/v{version}/{resource}/{id}/{sub-resource}
```

### HTTP Methods
| Method | Usage | Idempotent |
|--------|-------|------------|
| GET | Read resource(s) | Yes |
| POST | Create resource or trigger action | No |
| PATCH | Partial update | Yes |
| DELETE | Remove resource | Yes |

PUT is not used — PATCH for all updates.

### Versioning
- URL-based versioning: `/api/v1/`, `/api/v2/`
- Breaking changes increment version
- Previous version supported for 6 months after deprecation

## Request/Response Format

### Successful Response
```json
// Single resource
{
  "data": { ... }
}

// Collection (paginated)
{
  "data": [ ... ],
  "pagination": {
    "cursor": "eyJpZCI6Ij...",
    "hasMore": true,
    "totalCount": 142
  }
}
```

### Error Response
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Human-readable description",
    "details": [
      {
        "field": "title",
        "message": "Title must be between 5 and 200 characters",
        "rejectedValue": "Hi"
      }
    ],
    "traceId": "abc-123-def"
  }
}
```

### Standard Error Codes
| HTTP Status | Error Code | When |
|-------------|-----------|------|
| 400 | `VALIDATION_ERROR` | Request body fails validation |
| 401 | `UNAUTHORIZED` | Missing or invalid JWT |
| 403 | `FORBIDDEN` | Valid JWT but insufficient role |
| 404 | `NOT_FOUND` | Resource doesn't exist |
| 409 | `CONFLICT` | Duplicate resource (e.g., display name taken) |
| 422 | `BUSINESS_RULE_VIOLATION` | Domain invariant violated |
| 429 | `RATE_LIMITED` | Too many requests |
| 500 | `INTERNAL_ERROR` | Unexpected server error |

## Pagination
- **Cursor-based** for all list endpoints (no offset-based)
- Default page size: 20
- Max page size: 50
- Cursor is an opaque, base64-encoded string

```
GET /api/v1/threads?limit=20&cursor=eyJpZCI6Ij...&sort=latest
```

## Authentication
- JWT Bearer token in `Authorization` header
- `Authorization: Bearer <access-token>`
- API Gateway validates and extracts claims, forwards as headers:
  - `X-User-Anonymous-Id`: the user's anonymous identifier
  - `X-User-Roles`: comma-separated roles

## Rate Limiting
- Default: 100 requests/minute per user
- Write operations: 20 requests/minute per user
- Expert questions: 2 per week per user
- Rate limit headers returned:
  - `X-RateLimit-Limit`
  - `X-RateLimit-Remaining`
  - `X-RateLimit-Reset`

## CORS
- Allowed origins: configured per environment
- Allowed methods: GET, POST, PATCH, DELETE, OPTIONS
- Allowed headers: Authorization, Content-Type
- Max age: 3600 seconds

## Content Type
- All requests/responses: `application/json`
- Character encoding: UTF-8
- Date format: ISO 8601 (`2026-04-05T10:30:00Z`)
- UUIDs: lowercase with hyphens (`550e8400-e29b-41d4-a716-446655440000`)
