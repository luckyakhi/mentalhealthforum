# Event-Driven Architecture

## Principles
1. Services communicate via **domain events** published to Kafka topics
2. Events are **immutable facts** — something that happened, not a command
3. Events use **CloudEvents** envelope format (v1.0)
4. Each service **owns its events** — only the source service defines the schema
5. Consumers are **idempotent** — processing the same event twice has no side effect

## Event Catalog

### User Service Events

| Event | Topic | Trigger | Payload |
|-------|-------|---------|---------|
| `UserRegistered` | `user.events` | New user completes registration | `{userId, anonymousId, roles, timestamp}` |
| `UserBanned` | `user.events` | Admin bans a user | `{userId, anonymousId, reason, bannedBy, timestamp}` |
| `UserDeleted` | `user.events` | User requests account deletion | `{userId, anonymousId, timestamp}` |

### Forum Service Events

| Event | Topic | Trigger | Payload |
|-------|-------|---------|---------|
| `ThreadCreated` | `forum.events` | User creates a new thread | `{threadId, authorId, categoryId, title, body, timestamp}` |
| `ReplyCreated` | `forum.events` | User replies to a thread | `{replyId, threadId, authorId, body, timestamp}` |
| `ThreadClosed` | `forum.events` | Thread is closed by author/mod | `{threadId, closedBy, reason, timestamp}` |

### Moderation Service Events

| Event | Topic | Trigger | Payload |
|-------|-------|---------|---------|
| `ContentFlagged` | `moderation.events` | AI or user flags content | `{contentId, contentType, flagType, confidence, timestamp}` |
| `ContentRemoved` | `moderation.events` | Moderator removes content | `{contentId, contentType, removedBy, reason, timestamp}` |
| `CrisisDetected` | `moderation.events` | Crisis keywords/AI detected | `{contentId, authorId, severity, timestamp}` |

### Notification Service (Consumer Only — Phase 1)

| Consumes | Action |
|----------|--------|
| `ReplyCreated` | Notify thread author |
| `ContentFlagged` | Notify moderators |
| `CrisisDetected` | Alert on-call moderator (urgent) |
| `UserBanned` | Notify user via email |

## Event Schema (CloudEvents Envelope)

```json
{
  "specversion": "1.0",
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "source": "forum-service",
  "type": "com.mentalhealthforum.forum.ThreadCreated",
  "datacontenttype": "application/json",
  "time": "2026-04-05T10:30:00Z",
  "data": {
    "threadId": "uuid",
    "authorId": "anon-xxxxx",
    "categoryId": "uuid",
    "title": "...",
    "body": "...",
    "timestamp": "2026-04-05T10:30:00Z"
  }
}
```

## Kafka Topic Naming Convention
```
<service-name>.events          # Domain events
<service-name>.commands        # Command messages (rare, only when needed)
<service-name>.deadletter      # Failed messages after max retries
```

## Consumer Group Naming
```
<consuming-service>.<topic-name>.consumer
```
Example: `moderation-service.forum.events.consumer`

## Error Handling
1. Consumer retries: 3 attempts with exponential backoff (1s, 5s, 25s)
2. After max retries → message goes to dead letter topic
3. Dead letter topics are monitored and alerting is configured
4. Manual replay tooling available for dead letter messages

## Local Development
For local development, use **Redpanda** (Kafka-compatible, lighter weight) via Docker Compose or k8s.
