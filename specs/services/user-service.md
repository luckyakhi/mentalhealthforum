# Service Spec: User Service

> **Status**: Approved
> **Owner**: Core Team
> **Phase**: 1 (MVP)
> **Last Updated**: 2026-04-05

## Purpose
Manages user identity, authentication, and profiles. The critical responsibility is maintaining the wall between real identity (email) and anonymous forum identity (display name, anonymous ID). This service is the ONLY place where PII exists.

## Domain Model

### Aggregate Roots

**User**
```
User {
    id: UserId (UUID)
    email: EncryptedEmail        # encrypted, never leaves this service
    passwordHash: PasswordHash   # BCrypt
    anonymousId: AnonymousId     # "anon-" + 5 random chars, immutable
    displayName: DisplayName     # user-chosen, mutable, unique
    roles: Set<Role>             # SEEKER, EXPERT, MODERATOR, ADMIN
    status: UserStatus           # ACTIVE, SUSPENDED, BANNED, DELETED
    notificationPreferences: NotificationPreferences
    createdAt: Instant
    updatedAt: Instant
}
```

**Invariants:**
- Email must be unique (checked via email hash)
- Display name must be unique, 3-50 characters, alphanumeric + underscores
- Anonymous ID is generated once at registration and NEVER changes
- A banned user cannot log in
- A deleted user's PII is purged, posts are anonymized

### Value Objects
- `UserId` — UUID wrapper
- `AnonymousId` — `anon-{random5}`, immutable after creation
- `DisplayName` — validated string (3-50 chars, `[a-zA-Z0-9_]+`)
- `EncryptedEmail` — AES-256-GCM encrypted bytes
- `PasswordHash` — BCrypt hash
- `Role` — enum: `SEEKER`, `EXPERT`, `MODERATOR`, `ADMIN`
- `UserStatus` — enum: `ACTIVE`, `SUSPENDED`, `BANNED`, `DELETED`
- `NotificationPreferences` — `{emailDigest: bool, mentions: bool, replies: bool}`

### Domain Events (Published)
| Event | When | Payload |
|-------|------|---------|
| `UserRegistered` | Registration complete | `{userId, anonymousId, roles}` |
| `UserBanned` | Admin bans user | `{userId, anonymousId, reason, bannedBy}` |
| `UserDeleted` | User requests deletion | `{userId, anonymousId}` |
| `UserRoleChanged` | Role added/removed | `{userId, anonymousId, oldRoles, newRoles}` |

### Domain Events (Consumed)
None — User Service is the identity source of truth.

## API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/v1/auth/register` | Register new user | No |
| POST | `/api/v1/auth/login` | Login, get JWT | No |
| POST | `/api/v1/auth/refresh` | Refresh access token | Refresh token |
| POST | `/api/v1/auth/logout` | Invalidate refresh token | Yes |
| GET | `/api/v1/users/me` | Get current user profile | Yes |
| PATCH | `/api/v1/users/me` | Update display name, preferences | Yes |
| DELETE | `/api/v1/users/me` | Request account deletion | Yes |
| GET | `/api/v1/users/{anonymousId}` | Get public profile (display name only) | No |
| POST | `/api/v1/admin/users/{userId}/ban` | Ban a user | ADMIN |
| POST | `/api/v1/admin/users/{userId}/unban` | Unban a user | ADMIN |

## Authentication Flow

```
1. User registers → email verified → JWT issued
2. JWT access token (15 min) contains: {sub: anonymousId, roles: [...], exp: ...}
3. JWT refresh token (7 days) stored in HttpOnly cookie + DB
4. API Gateway validates JWT signature, forwards anonymousId in header
5. Downstream services NEVER see email — only anonymousId
```

## Database Tables
See: [Data Architecture — User Service Database](../architecture/data-architecture.md#user-service-database-user_db)

## Dependencies
- **Inbound**: API Gateway (auth validation), all services (token verification via shared JWT secret)
- **Outbound**: Kafka (event publishing), Redis (rate limiting, token blacklist)

## Non-Functional Requirements
- **Latency**: p99 < 200ms for login/register
- **Throughput**: 100 requests/second (MVP)
- **Availability**: 99.9%
- **Data Retention**: Active accounts indefinite; deleted accounts PII purged in 30 days

## Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | HMAC-SHA256 signing key | — (required) |
| `JWT_ACCESS_EXPIRY` | Access token TTL | `15m` |
| `JWT_REFRESH_EXPIRY` | Refresh token TTL | `7d` |
| `ENCRYPTION_KEY` | AES-256 key for email encryption | — (required) |
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/user_db` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092` |
| `REDIS_URL` | Redis connection URL | `redis://localhost:6379` |

## Acceptance Criteria
See: [User Stories — Epic 1](../product/user-stories.md#epic-1-user-identity--authentication)
