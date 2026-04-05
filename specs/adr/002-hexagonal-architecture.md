# ADR-002: Hexagonal Architecture (Ports & Adapters)

**Status**: Accepted
**Date**: 2026-04-05
**Decision Makers**: Core Team

## Context
We need an architecture pattern that keeps domain logic pure, testable, and independent of infrastructure choices (database, messaging, web framework).

## Decision
Adopt **hexagonal architecture** (ports and adapters) for all microservices, combined with **Domain-Driven Design** tactical patterns.

## Rationale
- Domain logic is the most valuable and stable part of the system
- Infrastructure (databases, message brokers) may change; domain rules don't
- Hexagonal architecture enforces separation via ports (interfaces) and adapters (implementations)
- DDD aggregate roots enforce business invariants at the domain level
- This pattern makes unit testing domain logic trivial (no mocks needed for infrastructure)

### Alternatives Considered
- **Layered architecture**: Simpler but domain leaks into service/controller layers
- **Clean architecture**: Very similar; hexagonal is more pragmatic with Spring Boot
- **CQRS/Event sourcing**: Powerful but over-engineered for MVP phase

## Consequences
- **Positive**: Domain logic is 100% framework-free and unit-testable
- **Positive**: Swapping PostgreSQL for another DB requires only new adapter
- **Positive**: Clear ownership boundaries between layers
- **Negative**: More files and packages per service (mapper classes, port interfaces)
- **Negative**: Learning curve for developers unfamiliar with DDD
- **Mitigation**: Service template and hexagonal architecture guide in specs
