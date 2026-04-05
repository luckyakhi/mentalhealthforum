# Data Architecture

## Database-per-Service Pattern
Each microservice owns its database schema. No service may directly access another service's database.

## Service Data Ownership

### User Service Database (`user_db`)

```sql
-- Core user identity (encrypted PII)
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email_encrypted BYTEA NOT NULL,           -- AES-256 encrypted
    email_hash      VARCHAR(64) NOT NULL UNIQUE, -- SHA-256 for lookups
    password_hash   VARCHAR(255) NOT NULL,     -- BCrypt
    anonymous_id    VARCHAR(20) NOT NULL UNIQUE, -- anon-xxxxx
    display_name    VARCHAR(50) NOT NULL UNIQUE,
    roles           VARCHAR(50)[] NOT NULL DEFAULT '{SEEKER}',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_anonymous_id ON users(anonymous_id);
CREATE INDEX idx_users_email_hash ON users(email_hash);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
```

### Forum Service Database (`forum_db`)

```sql
-- Categories
CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    slug        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    sort_order  INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Forum threads
CREATE TABLE threads (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id   VARCHAR(20) NOT NULL,          -- anonymous_id from user service
    category_id UUID NOT NULL REFERENCES categories(id),
    title       VARCHAR(200) NOT NULL,
    body        TEXT NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, CLOSED, REMOVED
    is_pinned   BOOLEAN NOT NULL DEFAULT FALSE,
    reply_count INT NOT NULL DEFAULT 0,
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Thread replies
CREATE TABLE replies (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    thread_id   UUID NOT NULL REFERENCES threads(id),
    parent_id   UUID REFERENCES replies(id),   -- for 1-level nesting
    author_id   VARCHAR(20) NOT NULL,          -- anonymous_id
    body        TEXT NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Full-text search index
CREATE INDEX idx_threads_search ON threads USING GIN(to_tsvector('english', title || ' ' || body));
CREATE INDEX idx_threads_category ON threads(category_id, last_activity_at DESC);
CREATE INDEX idx_threads_author ON threads(author_id);
CREATE INDEX idx_replies_thread ON replies(thread_id, created_at);
```

### Moderation Service Database (`moderation_db`)

```sql
-- Content flags (from users or AI)
CREATE TABLE content_flags (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id      UUID NOT NULL,
    content_type    VARCHAR(20) NOT NULL,      -- THREAD, REPLY
    flag_source     VARCHAR(20) NOT NULL,      -- USER, AI_SCANNER
    flag_type       VARCHAR(30) NOT NULL,      -- HARASSMENT, MISINFORMATION, SPAM, CRISIS, TOXIC
    reporter_id     VARCHAR(20),               -- null for AI flags
    description     TEXT,
    confidence      DECIMAL(4,3),              -- AI confidence score (0.000-1.000)
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, DISMISSED
    reviewed_by     VARCHAR(20),
    reviewed_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Moderation audit log (immutable)
CREATE TABLE moderation_audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id      UUID NOT NULL,
    content_type    VARCHAR(20) NOT NULL,
    action          VARCHAR(30) NOT NULL,      -- REMOVED, WARNING_SENT, USER_BANNED, APPROVED
    performed_by    VARCHAR(20) NOT NULL,
    reason          TEXT NOT NULL,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_flags_status ON content_flags(status, created_at);
CREATE INDEX idx_flags_content ON content_flags(content_id, content_type);
CREATE INDEX idx_audit_content ON moderation_audit_log(content_id);
```

## Data Privacy Requirements

### Encryption
- User emails: AES-256-GCM encryption at rest, encryption key in AWS Secrets Manager
- Email lookup: SHA-256 hash stored separately for login lookups
- All databases: TLS in transit, encrypted at rest (AWS RDS encryption)

### Data Retention
- Active user data: retained while account is active
- Deleted user: PII purged within 30 days, posts anonymized to `[deleted-user]`
- Moderation audit logs: retained for 2 years (compliance)
- Refresh tokens: auto-purged on expiry

### GDPR/Privacy
- Right to be forgotten: user deletion cascades through all services via `UserDeleted` event
- Data export: user can export their posts and profile (anonymized)
- Consent: explicit consent for email notifications, tracked in user preferences

## Database Migration Strategy
- **Tool**: Flyway (Spring Boot integration)
- **Naming**: `V{version}__{description}.sql` (e.g., `V1__create_users_table.sql`)
- **Rules**: Migrations are forward-only, never edit an applied migration
- **Environments**: Each service runs its own migrations on startup
