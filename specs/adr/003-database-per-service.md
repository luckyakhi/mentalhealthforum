# ADR-003: Database-per-Service Pattern

**Status**: Accepted
**Date**: 2026-04-05
**Decision Makers**: Core Team

## Context
In a microservices architecture, we need to decide whether services share a database or each service owns its own.

## Decision
Each microservice **owns its own database schema**. No service may directly read or write another service's tables.

## Rationale
- **Loose coupling**: Services can evolve their schemas independently
- **Independent deployment**: Schema changes don't block other services
- **Data ownership**: Clear boundaries for GDPR compliance (User Service owns PII)
- **Technology freedom**: Each service could use a different database if needed

### Data Sharing
Cross-service data needs are met via:
1. **Events**: Service publishes domain events, consumers maintain local projections
2. **API calls** (via gateway): For synchronous queries (rare, avoid where possible)

### Alternatives Considered
- **Shared database**: Simpler initially but creates tight coupling and deployment dependencies
- **Schema-per-service in shared instance**: Compromise for cost; acceptable for local dev

## Consequences
- **Positive**: Independent deployments and schema evolution
- **Positive**: Clear data ownership for privacy compliance
- **Negative**: Cross-service queries require eventual consistency
- **Negative**: Higher infrastructure cost (multiple database instances in production)
- **Mitigation**: Local dev uses single PostgreSQL instance with multiple databases
