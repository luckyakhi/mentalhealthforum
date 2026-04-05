# Observability Standards

## Three Pillars

### 1. Logging
- **Framework**: SLF4J + Logback (Spring Boot default)
- **Format**: Structured JSON in production, human-readable in local dev
- **Correlation**: Every log entry includes `traceId` for request tracing
- **Levels**: ERROR (alerts), WARN (investigate), INFO (audit), DEBUG (dev only)
- **Sensitive Data**: NEVER log PII, email, passwords, or mental health content body text

```json
{
  "timestamp": "2026-04-05T10:30:00Z",
  "level": "INFO",
  "service": "forum-service",
  "traceId": "abc-123",
  "spanId": "def-456",
  "message": "Thread created",
  "threadId": "uuid",
  "categoryId": "uuid",
  "authorId": "anon-xxxxx"
}
```

### 2. Metrics
- **Framework**: Micrometer + Prometheus
- **Endpoint**: `/actuator/prometheus`
- **Standard Metrics**:
  - `http_server_requests_seconds` (histogram) — request latency by path, method, status
  - `jvm_memory_used_bytes` — JVM heap usage
  - `db_pool_active_connections` — database connection pool
  - `kafka_consumer_lag` — consumer group lag
- **Custom Metrics**:
  - `forum_threads_created_total` — counter
  - `moderation_flags_total{type=CRISIS|TOXIC|...}` — counter
  - `moderation_scan_duration_seconds` — histogram
  - `ai_draft_generation_seconds` — histogram

### 3. Distributed Tracing
- **Framework**: OpenTelemetry SDK + Spring Cloud Sleuth (auto-instrumentation)
- **Backend**: Jaeger (local) / AWS X-Ray (production)
- **Propagation**: W3C TraceContext headers
- **Sampling**: 100% local, 10% production (adjustable)

## Health Checks
Every service exposes Spring Boot Actuator endpoints:
- `/actuator/health` — liveness (is the process running?)
- `/actuator/health/readiness` — readiness (can it serve traffic?)
- `/actuator/info` — version, build info
- `/actuator/prometheus` — Prometheus metrics

## Alerting Rules (Production)

| Alert | Condition | Severity |
|-------|-----------|----------|
| Service Down | health check fails for >1 min | CRITICAL |
| High Error Rate | 5xx rate >5% for 5 min | HIGH |
| High Latency | p99 >2s for 5 min | HIGH |
| Crisis Content Unreviewed | CRISIS flag pending >30 min | CRITICAL |
| Kafka Consumer Lag | lag >1000 messages for 10 min | MEDIUM |
| Database Connection Pool | >80% utilization for 5 min | MEDIUM |
| Disk Space | >85% used | MEDIUM |

## Local Observability Stack
For local development, deploy lightweight observability via Docker Compose:
- **Prometheus** — metrics collection
- **Grafana** — dashboards (pre-built dashboards in `infrastructure/grafana/`)
- **Jaeger** — distributed tracing UI
