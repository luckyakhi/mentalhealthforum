# Service Spec: Notification Service

> **Status**: Approved
> **Owner**: Core Team
> **Phase**: 1 (basic) → Phase 3 (full)
> **Last Updated**: 2026-04-05

## Purpose
Handles all outbound notifications — email, in-app, and push. Consumes events from other services and delivers notifications based on user preferences. Never originates events; it is a pure consumer.

## Domain Model

### Aggregate Roots

**Notification**
```
Notification {
    id: NotificationId (UUID)
    recipientId: AnonymousAuthorId
    type: NotificationType           # REPLY, MENTION, MODERATION, CRISIS_ALERT, SYSTEM
    channel: NotificationChannel     # EMAIL, IN_APP, PUSH
    title: String
    body: String
    metadata: Map<String, String>    # link targets, content IDs, etc.
    status: NotificationStatus       # PENDING, SENT, FAILED, READ
    sentAt: Instant?
    readAt: Instant?
    createdAt: Instant
}
```

### Domain Events (Consumed)
| Event | Source | Action |
|-------|--------|--------|
| `ReplyCreated` | Forum | Notify thread author (if prefs enabled) |
| `ContentFlagged` | Moderation | Notify moderators on duty |
| `CrisisDetected` | Moderation | URGENT: notify all active moderators |
| `QuestionAnswered` | Expert | Notify seeker that answer is ready |
| `UserBanned` | User | Notify banned user via email |
| `ExpertVerified` | Expert | Notify expert of verification status |

## API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/notifications` | Get user's notifications (paginated) | Yes |
| PATCH | `/api/v1/notifications/{id}/read` | Mark as read | Yes |
| POST | `/api/v1/notifications/read-all` | Mark all as read | Yes |
| GET | `/api/v1/notifications/unread-count` | Get unread count | Yes |

## Dependencies
- **Inbound**: Kafka (all events), API Gateway (user queries)
- **Outbound**: Email provider (SendGrid/SES), PostgreSQL

## Non-Functional Requirements
- **Email Delivery**: <5 minutes from event
- **Crisis Alerts**: <1 minute from detection to moderator notification
- **In-App**: Real-time via WebSocket (Phase 2+)
