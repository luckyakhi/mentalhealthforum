# Security Standards

## Threat Model Context
This platform handles **mental health data** — one of the most sensitive categories of personal information. A data breach could:
- Expose someone's mental health struggles to employers, family, public
- Endanger individuals in crisis whose identity is revealed
- Violate healthcare privacy regulations

Security is not optional — it is a core product requirement.

## Authentication & Authorization

### JWT Security
- **Algorithm**: HMAC-SHA256 (symmetric) for MVP; migrate to RS256 (asymmetric) for multi-tenant
- **Access token TTL**: 15 minutes (short-lived)
- **Refresh token TTL**: 7 days, stored HttpOnly cookie + database
- **Token content**: `{sub: anonymousId, roles: [...], exp: ...}` — NEVER include email or PII
- **Rotation**: Refresh tokens are single-use (rotated on each refresh)
- **Revocation**: Logout invalidates refresh token in database

### Password Policy
- Minimum 8 characters
- At least 1 uppercase, 1 lowercase, 1 digit
- BCrypt hashing with cost factor 12
- Account lockout after 5 failed attempts (15 minute cooldown)
- No password hints or security questions

### Role-Based Access Control
| Role | Permissions |
|------|------------|
| `SEEKER` | Read/write own threads and replies, report content |
| `EXPERT` | All SEEKER + answer questions, manage own expert profile |
| `MODERATOR` | All SEEKER + review mod queue, remove content, warn users |
| `ADMIN` | All permissions + ban/unban users, manage categories, view audit logs |

## Data Protection

### Encryption at Rest
| Data | Method | Key Management |
|------|--------|---------------|
| User emails | AES-256-GCM (application-level) | AWS Secrets Manager |
| Database | PostgreSQL TDE / RDS encryption | AWS KMS |
| Backups | Encrypted | AWS KMS |
| Kafka messages | Encrypted volumes | AWS KMS |

### Encryption in Transit
- TLS 1.3 for all external traffic (ALB termination)
- TLS 1.2+ for internal service-to-service (within VPC)
- TLS for database connections (RDS enforced)

### Data Minimization
- Collect only what's needed: email for auth, anonymous ID for everything else
- No tracking, analytics cookies, or behavioral profiling
- No IP address logging (beyond rate limiting)
- Forum posts store anonymous ID only — no way to link to email without User Service access

## OWASP Top 10 Mitigations

| Threat | Mitigation |
|--------|-----------|
| **Injection (SQL/NoSQL)** | Parameterized queries via JPA; no raw SQL concatenation |
| **Broken Authentication** | JWT with short TTL, refresh rotation, account lockout |
| **Sensitive Data Exposure** | Email encrypted, PII in one service, TLS everywhere |
| **XXE** | Jackson JSON (not XML), XML processing disabled |
| **Broken Access Control** | Role-based checks at gateway + service level |
| **Security Misconfiguration** | Actuator endpoints restricted, default passwords changed |
| **XSS** | React auto-escapes; Content-Security-Policy headers |
| **Insecure Deserialization** | No Java serialization; JSON with strict schemas |
| **Known Vulnerabilities** | Dependabot alerts, regular dependency updates |
| **Insufficient Logging** | Structured logging with trace IDs; audit log for mod actions |

## Input Validation
- Validate at API boundary (controller layer) using Jakarta Bean Validation
- Validate at domain boundary (value object constructors)
- Maximum input sizes enforced (title: 200, body: 10000, reply: 5000)
- Markdown sanitization: strip HTML, allow only safe markdown tags
- No file uploads in Phase 1

## Rate Limiting
- Per-user rate limits (by anonymous ID or IP for unauthenticated)
- Stricter limits on write operations
- Separate limits for auth endpoints (prevent brute force)
- Redis-backed sliding window counter

## Security Headers
```
Content-Security-Policy: default-src 'self'; script-src 'self'
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 0  (CSP replaces this)
Strict-Transport-Security: max-age=31536000; includeSubDomains
Referrer-Policy: no-referrer
Permissions-Policy: camera=(), microphone=(), geolocation=()
```

## Incident Response
- Security incidents escalated immediately (not just moderation queue)
- Data breach notification within 72 hours (GDPR requirement)
- Incident response runbook in ops documentation
- Annual security audit / penetration test

## Dependency Security
- Dependabot enabled for all repositories
- Critical CVEs patched within 48 hours
- High CVEs patched within 1 week
- Snyk or OWASP Dependency-Check in CI pipeline
