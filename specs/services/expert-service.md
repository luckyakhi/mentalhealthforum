# Service Spec: Expert Service

> **Status**: Draft
> **Owner**: Core Team
> **Phase**: 3
> **Last Updated**: 2026-04-05

## Purpose
Manages mental health expert profiles, credential verification, availability scheduling, and question routing. This service bridges the gap between anonymous seekers and verified professionals.

## Domain Model

### Aggregate Roots

**Expert**
```
Expert {
    id: ExpertId (UUID)
    userId: UserId                   # link to User Service account
    displayName: String              # professional name (not anonymous)
    credentials: List<Credential>    # license info
    specializations: Set<Specialization>  # ANXIETY, DEPRESSION, TRAUMA, etc.
    bio: String                      # professional bio (max 2000 chars)
    verificationStatus: VerificationStatus  # PENDING, VERIFIED, REJECTED
    availability: WeeklySchedule
    maxQuestionsPerDay: int          # rate limiting
    activeQuestionCount: int
    createdAt: Instant
}

Invariants:
- Expert must have at least one credential
- Only VERIFIED experts can receive questions
- activeQuestionCount cannot exceed maxQuestionsPerDay
```

**ExpertQuestion**
```
ExpertQuestion {
    id: QuestionId (UUID)
    seekerAnonymousId: AnonymousAuthorId
    expertId: ExpertId?              # null until assigned
    specialization: Specialization   # for routing
    question: String                 # max 5000 chars
    status: QuestionStatus           # PENDING, ASSIGNED, ANSWERED, CLOSED
    answer: String?
    aiDraftAnswer: String?           # Phase 4: AI draft
    createdAt: Instant
    answeredAt: Instant?
}
```

### Value Objects
- `Credential` — `{type, licenseNumber, state, expiryDate, verifiedAt}`
- `Specialization` — enum: `ANXIETY`, `DEPRESSION`, `TRAUMA`, `RELATIONSHIPS`, `SUBSTANCE_USE`, `EATING_DISORDERS`, `GRIEF`, `WORK_STRESS`, `GENERAL`
- `VerificationStatus` — enum: `PENDING`, `VERIFIED`, `REJECTED`
- `QuestionStatus` — enum: `PENDING`, `ASSIGNED`, `ANSWERED`, `CLOSED`
- `WeeklySchedule` — availability slots per day of week

### Domain Events (Published)
| Event | When | Payload |
|-------|------|---------|
| `ExpertVerified` | Admin verifies credentials | `{expertId, specializations}` |
| `QuestionAssigned` | Question routed to expert | `{questionId, expertId, seekerAnonymousId}` |
| `QuestionAnswered` | Expert submits answer | `{questionId, expertId, seekerAnonymousId}` |

### Domain Events (Consumed)
| Event | Source | Action |
|-------|--------|--------|
| `UserDeleted` | User Service | Remove expert profile if applicable |
| `UserBanned` | User Service | Deactivate expert profile |

## API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/v1/experts/register` | Register as expert | Yes (SEEKER) |
| GET | `/api/v1/experts/{expertId}` | Get expert profile | No |
| PATCH | `/api/v1/experts/{expertId}` | Update expert profile | Yes (owner) |
| GET | `/api/v1/experts` | List verified experts | No |
| POST | `/api/v1/questions` | Submit question to expert | Yes (SEEKER) |
| GET | `/api/v1/questions/{questionId}` | Get question + answer | Yes (seeker/expert) |
| POST | `/api/v1/questions/{questionId}/answer` | Answer question | Yes (EXPERT) |
| POST | `/api/v1/admin/experts/{expertId}/verify` | Verify expert | ADMIN |
| POST | `/api/v1/admin/experts/{expertId}/reject` | Reject expert | ADMIN |

## Dependencies
- **Inbound**: API Gateway
- **Outbound**: Kafka, PostgreSQL
- **Consumes events from**: User Service

## Non-Functional Requirements
- **Latency**: p99 < 200ms
- **Question Routing**: <1 minute from submission to expert assignment
- **Availability**: 99.9%

## Acceptance Criteria
See: [User Stories — Epic 4](../product/user-stories.md#epic-4-expert-integration-phase-3)
