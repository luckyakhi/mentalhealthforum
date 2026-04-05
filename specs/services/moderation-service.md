# Service Spec: Moderation Service

> **Status**: Approved
> **Owner**: Core Team
> **Phase**: 1 (basic keyword) → Phase 2 (AI-powered)
> **Last Updated**: 2026-04-05

## Purpose
Ensures platform safety by scanning all user-generated content for harmful material, managing a moderation queue, and providing crisis detection. Phase 1 uses keyword matching; Phase 2 adds AI-powered content classification via Claude API.

## Domain Model

### Aggregate Roots

**ContentFlag**
```
ContentFlag {
    id: FlagId (UUID)
    contentId: ContentId             # thread or reply UUID
    contentType: ContentType         # THREAD, REPLY
    flagSource: FlagSource           # USER_REPORT, AI_SCANNER, KEYWORD_SCANNER
    flagType: FlagType               # HARASSMENT, MISINFORMATION, SPAM, CRISIS, TOXIC, OTHER
    reporterId: AnonymousAuthorId?   # null for automated flags
    description: String?             # user-provided context
    confidence: BigDecimal?          # AI confidence (0.000-1.000)
    status: FlagStatus               # PENDING, APPROVED, DISMISSED
    reviewedBy: String?              # moderator who reviewed
    reviewedAt: Instant?
    createdAt: Instant
}

Invariants:
- AI flags must have a confidence score
- User reports must have a reporterId
- Once reviewed (APPROVED/DISMISSED), status cannot change
- Duplicate flags on same content from same reporter are rejected
```

**ModerationAction** (audit log, immutable)
```
ModerationAction {
    id: ActionId (UUID)
    contentId: ContentId
    contentType: ContentType
    action: ActionType               # CONTENT_REMOVED, WARNING_SENT, USER_BANNED, CONTENT_APPROVED
    performedBy: String              # moderator anonymous ID
    reason: String
    metadata: Map<String, Object>    # flexible additional data
    createdAt: Instant
}

Invariants:
- Immutable after creation (audit trail)
- Must always have a reason
```

### Value Objects
- `FlagId`, `ActionId`, `ContentId` — UUID wrappers
- `ContentType` — enum: `THREAD`, `REPLY`
- `FlagSource` — enum: `USER_REPORT`, `AI_SCANNER`, `KEYWORD_SCANNER`
- `FlagType` — enum: `HARASSMENT`, `MISINFORMATION`, `SPAM`, `CRISIS`, `TOXIC`, `OTHER`
- `FlagStatus` — enum: `PENDING`, `APPROVED`, `DISMISSED`
- `ActionType` — enum: `CONTENT_REMOVED`, `WARNING_SENT`, `USER_BANNED`, `CONTENT_APPROVED`

### Domain Events (Published)
| Event | When | Payload |
|-------|------|---------|
| `ContentFlagged` | Content flagged by any source | `{contentId, contentType, flagType, confidence}` |
| `ContentRemoved` | Moderator removes content | `{contentId, contentType, removedBy, reason}` |
| `CrisisDetected` | Crisis content detected | `{contentId, authorId, severity}` |
| `UserWarned` | Moderator issues warning | `{userId, reason}` |

### Domain Events (Consumed)
| Event | Source | Action |
|-------|--------|--------|
| `ThreadCreated` | Forum Service | Trigger content scan |
| `ReplyCreated` | Forum Service | Trigger content scan |

## Content Scanning Pipeline

### Phase 1: Keyword Scanner
```
Input: content text
  → Normalize (lowercase, strip special chars)
  → Match against crisis keyword list
  → Match against toxicity keyword list
  → Output: {flagType, confidence: 1.0 for exact match}
```

**Crisis Keywords:**
`suicide`, `kill myself`, `end it all`, `self-harm`, `cutting`, `overdose`,
`don't want to live`, `no reason to live`, `want to die`, `end my life`

### Phase 2: AI Scanner (Claude API)
```
Input: content text + context (thread title, category)
  → Send to Claude API with structured prompt
  → Parse classification: {SAFE, NEEDS_REVIEW, CRISIS, TOXIC}
  → Output: {flagType, confidence: 0.0-1.0}

Prompt template:
  "Classify this mental health forum post for content safety.
   Categories: SAFE, NEEDS_REVIEW (borderline), CRISIS (immediate danger), TOXIC (harmful).
   Consider context: this is a mental health support forum where discussing
   difficult emotions is expected and healthy.
   Respond with JSON: {classification, confidence, reasoning}"
```

## API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/moderation/queue` | Get pending flags (paginated) | MODERATOR |
| GET | `/api/v1/moderation/queue/{flagId}` | Get flag details | MODERATOR |
| POST | `/api/v1/moderation/queue/{flagId}/review` | Review flag (approve/dismiss) | MODERATOR |
| POST | `/api/v1/moderation/actions` | Take moderation action | MODERATOR |
| GET | `/api/v1/moderation/audit-log` | View audit log (paginated) | ADMIN |
| GET | `/api/v1/moderation/stats` | Moderation statistics | MODERATOR |

## Dependencies
- **Inbound**: API Gateway (moderator UI), Kafka (content events)
- **Outbound**: Kafka (moderation events), Claude API (Phase 2), PostgreSQL
- **Consumes events from**: Forum Service

## Non-Functional Requirements
- **Scan Latency**: <5 seconds for keyword scan, <15 seconds for AI scan
- **Crisis Detection**: <30 seconds from post creation to moderator alert
- **Queue SLA**: All CRISIS flags reviewed within 1 hour
- **Availability**: 99.9%

## Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL connection | `jdbc:postgresql://localhost:5432/moderation_db` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | `localhost:9092` |
| `CLAUDE_API_KEY` | Anthropic API key (Phase 2) | — |
| `CLAUDE_MODEL` | Claude model ID | `claude-haiku-4-5-20251001` |
| `SCAN_MODE` | `KEYWORD` or `AI` | `KEYWORD` |
| `CRISIS_KEYWORDS_PATH` | Path to crisis keywords file | `classpath:crisis-keywords.txt` |

## Acceptance Criteria
See: [User Stories — Epic 3](../product/user-stories.md#epic-3-content-moderation)
