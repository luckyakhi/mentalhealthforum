# ADR-001: Java 21 + Spring Boot 3.5.x

**Status**: Accepted
**Date**: 2026-04-05
**Decision Makers**: Core Team

## Context
We need a backend technology stack for a mental health platform that will handle sensitive data, require high reliability, and scale from MVP to production.

## Decision
Use **Java 21** (LTS) with **Spring Boot 3.5.x** as the primary backend framework.

## Rationale

### Why Java 21
- **LTS release** — long-term support, enterprise-grade stability
- **Virtual threads (Project Loom)** — efficient concurrency without reactive complexity
- **Records** — immutable domain objects with zero boilerplate
- **Sealed interfaces** — exhaustive pattern matching for domain enums
- **Pattern matching** — cleaner business logic code
- **Mature ecosystem** — security libraries, ORMs, testing frameworks

### Why Spring Boot 3.5.x
- Industry standard for Java microservices
- Spring Cloud ecosystem for service discovery, gateway, config
- Spring Security for authentication/authorization
- Spring Data JPA for database access
- Excellent Testcontainers integration
- Native GraalVM support for future optimization
- Massive community and documentation

### Alternatives Considered
- **Kotlin + Ktor**: More concise language, but smaller hiring pool and ecosystem
- **Go**: Great for performance, but less mature ORM/DDD ecosystem
- **Node.js/TypeScript**: Fast to prototype, but weaker type safety for complex domain logic
- **Quarkus**: Excellent performance, but smaller community and less ecosystem maturity

## Consequences
- **Positive**: Proven stack, large talent pool, extensive library ecosystem
- **Positive**: Virtual threads simplify async I/O without reactive programming
- **Negative**: More verbose than Kotlin or TypeScript
- **Negative**: JVM memory footprint higher than Go
- **Mitigation**: Use Java records and modern features to reduce boilerplate
