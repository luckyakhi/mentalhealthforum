# Service Spec: Suggestion Service

> **Status**: Draft
> **Owner**: Core Team
> **Phase**: 4
> **Last Updated**: 2026-04-05

## Purpose
Leverages AI (Claude API) to generate draft health suggestions for expert review, and manages a curated resource library. AI NEVER provides suggestions directly to users — all AI output must be reviewed and approved by a verified expert before delivery.

## Domain Model

### Aggregate Roots

**AiDraft**
```
AiDraft {
    id: DraftId (UUID)
    questionId: QuestionId
    expertId: ExpertId               # assigned reviewer
    generatedContent: String         # AI-generated response
    modelId: String                  # Claude model used
    confidenceScore: BigDecimal      # AI confidence
    status: DraftStatus              # GENERATED, APPROVED, EDITED, REJECTED
    expertEditedContent: String?     # expert's modified version
    reviewedAt: Instant?
    createdAt: Instant
}

Invariants:
- Draft is NEVER visible to seekers until status is APPROVED or EDITED
- Expert must provide reason when rejecting
- Version history is immutable (audit trail)
```

**Resource**
```
Resource {
    id: ResourceId (UUID)
    title: String
    type: ResourceType               # ARTICLE, WORKSHEET, EXERCISE, CRISIS_LINE, VIDEO
    category: Specialization
    content: String                  # or URL for external resources
    curatedBy: ExpertId              # expert who approved this resource
    tags: Set<String>
    status: ResourceStatus           # DRAFT, PUBLISHED, ARCHIVED
    createdAt: Instant
}
```

### Domain Events (Published)
| Event | When | Payload |
|-------|------|---------|
| `DraftGenerated` | AI draft created | `{draftId, questionId, expertId}` |
| `DraftApproved` | Expert approves draft | `{draftId, questionId}` |
| `ResourcePublished` | New resource published | `{resourceId, type, category}` |

### Domain Events (Consumed)
| Event | Source | Action |
|-------|--------|--------|
| `QuestionAssigned` | Expert Service | Trigger AI draft generation |

## AI Integration

### Draft Generation Prompt Template
```
System: You are a mental health support assistant. Generate a compassionate,
evidence-based response to a user's mental health question. This draft will
be reviewed by a licensed mental health professional before delivery.

Guidelines:
- Be empathetic and validating
- Reference evidence-based techniques (CBT, DBT, mindfulness) where appropriate
- Never diagnose or prescribe medication
- Always include a reminder that this is not a substitute for professional help
- If the question indicates crisis, prioritize safety resources

Context:
- Specialization area: {specialization}
- Expert's preferred style: {expertPreferences}

Question: {questionText}

Respond with:
1. A compassionate acknowledgment
2. Evidence-based suggestions (2-3 specific techniques)
3. Relevant self-help resources from our library
4. A gentle reminder about professional support
```

### AI Model Selection
- Primary: `claude-sonnet-4-6` (balance of quality and speed)
- Fallback: `claude-haiku-4-5-20251001` (if primary is unavailable)
- Temperature: 0.3 (consistent, grounded responses)
- Max tokens: 1500

## API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/drafts/{draftId}` | Get AI draft | EXPERT |
| POST | `/api/v1/drafts/{draftId}/approve` | Approve draft | EXPERT |
| POST | `/api/v1/drafts/{draftId}/edit` | Edit and approve | EXPERT |
| POST | `/api/v1/drafts/{draftId}/reject` | Reject draft | EXPERT |
| GET | `/api/v1/resources` | List resources (filtered) | No |
| GET | `/api/v1/resources/{resourceId}` | Get resource | No |
| POST | `/api/v1/resources` | Create resource | EXPERT |
| GET | `/api/v1/recommendations` | Get personalized recommendations | Yes |

## Dependencies
- **Inbound**: API Gateway, Expert Service (via events)
- **Outbound**: Claude API (Anthropic), Kafka, PostgreSQL
- **Consumes events from**: Expert Service

## Non-Functional Requirements
- **Draft Generation**: <30 seconds
- **Availability**: 99.5% (AI dependency)
- **Fallback**: If AI unavailable, experts write responses manually (graceful degradation)

## Acceptance Criteria
See: [User Stories — Epic 5](../product/user-stories.md#epic-5-ai-assisted-suggestions-phase-4)
