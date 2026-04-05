# System Architecture Overview

## C4 Context Diagram

```mermaid
C4Context
    title Mental Health Platform — System Context

    Person(seeker, "Seeker", "Individual seeking mental health support")
    Person(expert, "Expert", "Licensed mental health professional")
    Person(admin, "Admin", "Platform administrator/moderator")

    System(platform, "Mental Health Platform", "Anonymous forum + expert guidance + AI-assisted suggestions")

    System_Ext(email, "Email Service", "SendGrid / AWS SES")
    System_Ext(ai, "AI/LLM Service", "Claude API for content analysis & draft suggestions")
    System_Ext(idp, "Identity Provider", "Google OAuth (optional)")

    Rel(seeker, platform, "Browses forum, creates threads, asks experts")
    Rel(expert, platform, "Reviews questions, approves AI drafts")
    Rel(admin, platform, "Moderates content, manages users")
    Rel(platform, email, "Sends notifications")
    Rel(platform, ai, "Content moderation, draft suggestions")
    Rel(platform, idp, "Optional social login")
```

## C4 Container Diagram

```mermaid
C4Container
    title Mental Health Platform — Container View

    Person(user, "User", "Seeker / Expert / Admin")

    Container_Boundary(frontend, "Frontend") {
        Container(spa, "Web Application", "React 19, TypeScript", "Single-page application")
    }

    Container_Boundary(gateway, "API Layer") {
        Container(apigw, "API Gateway", "Spring Cloud Gateway", "Routing, rate limiting, auth validation")
    }

    Container_Boundary(services, "Microservices") {
        Container(usersvc, "User Service", "Spring Boot 3.5", "Authentication, profiles, anonymous identity")
        Container(forumsvc, "Forum Service", "Spring Boot 3.5", "Threads, replies, categories, search")
        Container(modsvc, "Moderation Service", "Spring Boot 3.5", "Content safety, AI moderation, queue")
        Container(expertsvc, "Expert Service", "Spring Boot 3.5", "Expert profiles, credentials, matching")
        Container(suggestsvc, "Suggestion Service", "Spring Boot 3.5", "AI draft suggestions, resource recommendations")
        Container(notifysvc, "Notification Service", "Spring Boot 3.5", "Email, push, in-app notifications")
    }

    Container_Boundary(data, "Data Stores") {
        ContainerDb(userdb, "User DB", "PostgreSQL", "User accounts, credentials")
        ContainerDb(forumdb, "Forum DB", "PostgreSQL", "Threads, replies, categories")
        ContainerDb(moddb, "Moderation DB", "PostgreSQL", "Flags, queue, audit log")
        ContainerDb(expertdb, "Expert DB", "PostgreSQL", "Expert profiles, credentials")
        ContainerDb(suggestdb, "Suggestion DB", "PostgreSQL", "Drafts, resources, recommendations")
        ContainerDb(cache, "Cache", "Redis", "Sessions, rate limiting, hot data")
    }

    Container_Boundary(messaging, "Messaging") {
        Container(kafka, "Event Bus", "Apache Kafka", "Async inter-service communication")
    }

    Rel(user, spa, "HTTPS")
    Rel(spa, apigw, "REST/JSON")
    Rel(apigw, usersvc, "HTTP")
    Rel(apigw, forumsvc, "HTTP")
    Rel(apigw, modsvc, "HTTP")
    Rel(apigw, expertsvc, "HTTP")
    Rel(apigw, suggestsvc, "HTTP")

    Rel(usersvc, userdb, "JDBC")
    Rel(forumsvc, forumdb, "JDBC")
    Rel(modsvc, moddb, "JDBC")
    Rel(expertsvc, expertdb, "JDBC")
    Rel(suggestsvc, suggestdb, "JDBC")

    Rel(usersvc, kafka, "Publishes: UserRegistered, UserBanned")
    Rel(forumsvc, kafka, "Publishes: ThreadCreated, ReplyCreated")
    Rel(modsvc, kafka, "Consumes: ThreadCreated, ReplyCreated; Publishes: ContentFlagged")
    Rel(notifysvc, kafka, "Consumes: all notification-worthy events")
```

## Service Responsibilities

| Service | Owns | Phase |
|---------|------|-------|
| **User Service** | Registration, auth, JWT, profiles, anonymous identity | Phase 1 |
| **Forum Service** | Categories, threads, replies, search | Phase 1 |
| **Moderation Service** | Content scanning, flag queue, moderation actions, audit log | Phase 1 (basic) → Phase 2 (AI) |
| **Expert Service** | Expert onboarding, credential verification, availability, matching | Phase 3 |
| **Suggestion Service** | AI draft generation, expert review workflow, resource library | Phase 4 |
| **Notification Service** | Email digests, mention alerts, crisis alerts, push notifications | Phase 1 (basic) → Phase 3 (full) |
| **API Gateway** | Routing, auth token validation, rate limiting, CORS | Phase 1 |

## Inter-Service Communication

All service-to-service communication is **event-driven via Kafka**. No direct HTTP calls between services.

See: [Event-Driven Architecture](event-driven.md)

## Technology Stack Summary

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21+ |
| Framework | Spring Boot | 3.5.x |
| API Gateway | Spring Cloud Gateway | 2024.x |
| Database | PostgreSQL | 16 |
| Cache | Redis | 7.x |
| Messaging | Apache Kafka | 3.7+ |
| Search | PostgreSQL Full-Text (Phase 1) → Elasticsearch (Phase 3+) | — |
| AI/LLM | Claude API (Anthropic) | Latest |
| Container Runtime | Docker | 24+ |
| Local Orchestration | Kubernetes (Docker Desktop) | 1.29+ |
| Production Orchestration | AWS ECS Fargate | — |
| IaC | Terraform | 1.7+ |
| CI/CD | GitHub Actions | — |
| Monitoring | Prometheus + Grafana | — |
| Tracing | OpenTelemetry + Jaeger | — |
