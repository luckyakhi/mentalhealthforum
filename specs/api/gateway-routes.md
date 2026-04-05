# API Gateway Routing

## Technology
Spring Cloud Gateway (reactive) running on port **8080**.

## Route Configuration

| Route | Target Service | Port | Auth Required | Rate Limit |
|-------|---------------|------|---------------|------------|
| `/api/v1/auth/**` | user-service | 8081 | No | 20 req/min |
| `/api/v1/users/**` | user-service | 8081 | Varies | 100 req/min |
| `/api/v1/admin/users/**` | user-service | 8081 | ADMIN | 50 req/min |
| `/api/v1/categories/**` | forum-service | 8082 | No | 200 req/min |
| `/api/v1/threads/**` | forum-service | 8082 | Varies | 100 req/min |
| `/api/v1/replies/**` | forum-service | 8082 | Varies | 100 req/min |
| `/api/v1/search/**` | forum-service | 8082 | No | 50 req/min |
| `/api/v1/moderation/**` | moderation-service | 8083 | MODERATOR+ | 100 req/min |
| `/api/v1/experts/**` | expert-service | 8084 | Varies | 100 req/min |
| `/api/v1/questions/**` | expert-service | 8084 | Yes | 10 req/min |
| `/api/v1/drafts/**` | suggestion-service | 8085 | EXPERT | 50 req/min |
| `/api/v1/resources/**` | suggestion-service | 8085 | Varies | 100 req/min |
| `/api/v1/notifications/**` | notification-service | 8086 | Yes | 100 req/min |

## Gateway Responsibilities

### 1. JWT Validation
- Validate JWT signature using shared secret
- Extract claims and forward as headers:
  - `X-User-Anonymous-Id`
  - `X-User-Roles`
  - `X-User-Id` (internal UUID, for admin operations)
- Reject expired tokens with 401

### 2. Rate Limiting
- Redis-backed rate limiter
- Key: user anonymous ID (or IP for unauthenticated)
- Per-route limits as specified above

### 3. CORS
- Configured per environment
- Local: `http://localhost:3000`
- Production: `https://mentalhealthforum.com`

### 4. Request/Response Logging
- Log: method, path, status code, latency, trace ID
- NEVER log request bodies (may contain sensitive mental health data)
- Trace ID propagated via `X-Trace-Id` header

### 5. Circuit Breaker
- Per-service circuit breaker (Resilience4j)
- Open after 5 failures in 30 seconds
- Half-open after 60 seconds
- Fallback: 503 Service Unavailable with retry-after header

## Port Assignments

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| User Service | 8081 |
| Forum Service | 8082 |
| Moderation Service | 8083 |
| Expert Service | 8084 |
| Suggestion Service | 8085 |
| Notification Service | 8086 |
