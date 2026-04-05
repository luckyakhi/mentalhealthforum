# Service Spec: Forum Service

> **Status**: Approved
> **Owner**: Core Team
> **Phase**: 1 (MVP)
> **Last Updated**: 2026-04-05

## Purpose
Manages the core forum functionality: categories, threads, and replies. This is the heart of Phase 1 — the anonymous discussion platform. Content is identified only by anonymous IDs; this service never handles PII.

## Domain Model

### Aggregate Roots

**ForumThread**
```
ForumThread {
    id: ThreadId (UUID)
    authorId: AnonymousAuthorId      # from JWT, never a real identity
    categoryId: CategoryId
    title: ThreadTitle               # 5-200 chars
    body: ThreadBody                 # 10-10000 chars, markdown
    status: ThreadStatus             # ACTIVE, CLOSED, REMOVED
    isPinned: boolean
    replyCount: int                  # denormalized for performance
    lastActivityAt: Instant
    replies: List<Reply>             # loaded lazily
    createdAt: Instant
    updatedAt: Instant
}

Invariants:
- Only ACTIVE threads accept new replies
- Only thread author or MODERATOR/ADMIN can close a thread
- REMOVED threads are not returned in queries (soft delete)
- replyCount is updated on every reply add/remove
```

**Category**
```
Category {
    id: CategoryId (UUID)
    name: CategoryName               # unique
    slug: CategorySlug               # URL-friendly, unique
    description: String
    sortOrder: int
    threadCount: int                 # denormalized
}
```

### Entities (within ForumThread aggregate)

**Reply**
```
Reply {
    id: ReplyId (UUID)
    threadId: ThreadId
    parentId: ReplyId?               # null = top-level, non-null = nested (1 level max)
    authorId: AnonymousAuthorId
    body: ReplyBody                  # 10-5000 chars
    status: ReplyStatus              # ACTIVE, REMOVED
    createdAt: Instant
    updatedAt: Instant
}
```

### Value Objects
- `ThreadId`, `ReplyId`, `CategoryId` — UUID wrappers
- `AnonymousAuthorId` — string wrapper matching `anon-[a-z0-9]{5}` pattern
- `ThreadTitle` — validated string (5-200 chars)
- `ThreadBody` — validated string (10-10000 chars)
- `ReplyBody` — validated string (10-5000 chars)
- `CategorySlug` — URL-safe string
- `ThreadStatus` — enum: `ACTIVE`, `CLOSED`, `REMOVED`
- `ReplyStatus` — enum: `ACTIVE`, `REMOVED`

### Domain Events (Published)
| Event | When | Payload |
|-------|------|---------|
| `ThreadCreated` | New thread saved | `{threadId, authorId, categoryId, title, body}` |
| `ReplyCreated` | New reply saved | `{replyId, threadId, authorId, body}` |
| `ThreadClosed` | Thread closed | `{threadId, closedBy, reason}` |
| `ContentRemoved` | Mod removes content | `{contentId, contentType, removedBy}` |

### Domain Events (Consumed)
| Event | Source | Action |
|-------|--------|--------|
| `UserBanned` | User Service | Close all active threads by user, remove from search |
| `UserDeleted` | User Service | Anonymize author to `[deleted-user]` on all posts |

## API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/categories` | List all categories | No |
| GET | `/api/v1/categories/{slug}` | Get category details | No |
| GET | `/api/v1/threads` | List threads (paginated, filterable by category) | No |
| GET | `/api/v1/threads/{threadId}` | Get thread with replies | No |
| POST | `/api/v1/threads` | Create thread | Yes |
| PATCH | `/api/v1/threads/{threadId}` | Update thread (close) | Yes (author/mod) |
| POST | `/api/v1/threads/{threadId}/replies` | Create reply | Yes |
| GET | `/api/v1/search` | Full-text search threads | No |
| POST | `/api/v1/threads/{threadId}/report` | Report thread | Yes |
| POST | `/api/v1/replies/{replyId}/report` | Report reply | Yes |

### Pagination
All list endpoints support cursor-based pagination:
```
GET /api/v1/threads?categoryId={uuid}&cursor={lastThreadId}&limit=20&sort=latest|active
```

### Search
```
GET /api/v1/search?q={query}&categoryId={uuid}&cursor={offset}&limit=20
```

## Database Tables
See: [Data Architecture — Forum Service Database](../architecture/data-architecture.md#forum-service-database-forum_db)

## Dependencies
- **Inbound**: API Gateway
- **Outbound**: Kafka (publish events), PostgreSQL (data store)
- **Consumes events from**: User Service

## Non-Functional Requirements
- **Latency**: p99 < 150ms for reads, < 300ms for writes
- **Throughput**: 500 reads/second, 50 writes/second (MVP)
- **Availability**: 99.9%
- **Search**: <500ms for full-text queries

## Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL connection | `jdbc:postgresql://localhost:5432/forum_db` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | `localhost:9092` |
| `MAX_THREAD_TITLE_LENGTH` | Title limit | `200` |
| `MAX_THREAD_BODY_LENGTH` | Body limit | `10000` |
| `MAX_REPLY_BODY_LENGTH` | Reply limit | `5000` |
| `DEFAULT_PAGE_SIZE` | Pagination default | `20` |
| `MAX_PAGE_SIZE` | Pagination max | `50` |

## Acceptance Criteria
See: [User Stories — Epic 2](../product/user-stories.md#epic-2-forum-discussions)
