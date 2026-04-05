# ADR-005: Anonymous Identity System

**Status**: Accepted
**Date**: 2026-04-05
**Decision Makers**: Core Team

## Context
Mental health discussions require anonymity to encourage honest sharing. Users must be able to participate without fear of identification, while the platform still needs accountability for moderation.

## Decision
Implement a **dual-identity system**: real identity (email) stored only in User Service, anonymous identity (`anon-xxxxx` + display name) used everywhere else.

## Design

```
User Service (PII boundary)          All Other Services
┌─────────────────────────┐          ┌────────────────────────┐
│ email (encrypted)       │          │ author_id: "anon-x7k2m"│
│ password_hash           │          │ display_name: "calm_w" │
│ anonymous_id: "anon-x7k"│    →     │                        │
│ display_name: "calm_w"  │  JWT     │ (no PII ever)          │
└─────────────────────────┘          └────────────────────────┘
```

### Key Rules
1. Anonymous ID is generated at registration, immutable, never changes
2. JWT token contains ONLY `{sub: anonymousId, roles: [...]}` — never email
3. API Gateway extracts anonymous ID from JWT and forwards as `X-User-Anonymous-Id` header
4. Forum, Moderation, Expert services NEVER receive or store email
5. Email is AES-256-GCM encrypted at rest; looked up via SHA-256 hash for login
6. Account deletion anonymizes all posts to `[deleted-user]` — not just the anonymous ID

## Rationale
- **Safety**: Users share more openly when they feel anonymous
- **Legal**: Minimizes PII exposure surface to one service
- **Moderation**: Anonymous IDs still enable banning and tracking repeat offenders
- **GDPR**: Right to be forgotten is simpler when PII lives in one place

### Alternatives Considered
- **Fully anonymous (no email)**: No accountability, no password recovery
- **Pseudonymous with real name optional**: Risk of accidental PII exposure
- **End-to-end encryption**: Prevents moderation, which is a safety requirement

## Consequences
- **Positive**: Strong privacy guarantees, reduced PII blast radius
- **Positive**: Simple GDPR compliance — delete one record, anonymize posts
- **Negative**: Cannot display "real" profiles for experts (solved by separate expert identity in Phase 3)
- **Negative**: Anonymous IDs are not human-memorable (display names compensate)
