# Specs Index — Mental Health Platform

This directory is the single source of truth for all platform specifications. AI coding agents and human developers alike MUST consult these specs before implementing any feature.

## How to Use These Specs

### For AI Coding Agents
1. **Before any implementation**: Read the relevant service spec + API contract
2. **Architecture questions**: Check ADRs first — they document WHY decisions were made
3. **Code style questions**: See `/specs/standards/`
4. **New service**: Follow the service template in `/specs/services/_service-template.md`

### For Human Developers
1. **New feature**: Write or update the spec FIRST, get review, then implement
2. **Architecture change**: Write an ADR, get consensus, then proceed
3. **API change**: Update the OpenAPI spec, regenerate contracts, then implement

## Directory Structure

```
specs/
├── SPECS-INDEX.md              ← You are here
├── product/
│   ├── vision.md               ← Product vision, target users, phased roadmap
│   └── user-stories.md         ← Epics and user stories with acceptance criteria
├── architecture/
│   ├── system-overview.md      ← C4 context & container diagrams (Mermaid)
│   ├── hexagonal-architecture.md ← DDD/Hexagonal pattern guide
│   ├── event-driven.md         ← Async messaging patterns & event catalog
│   └── data-architecture.md    ← Database-per-service, data ownership
├── services/
│   ├── _service-template.md    ← Template for new service specs
│   ├── user-service.md         ← Identity, auth, profiles
│   ├── forum-service.md        ← Threads, posts, anonymous discussions
│   ├── moderation-service.md   ← Content safety, AI moderation
│   ├── expert-service.md       ← Expert profiles, credentials, matching
│   ├── suggestion-service.md   ← AI-assisted health suggestions
│   └── notification-service.md ← Email, push, in-app notifications
├── api/
│   ├── api-guidelines.md       ← REST conventions, versioning, error format
│   ├── user-service-api.yaml   ← OpenAPI 3.1 contract
│   ├── forum-service-api.yaml  ← OpenAPI 3.1 contract
│   └── gateway-routes.md       ← API Gateway routing rules
├── infrastructure/
│   ├── local-k8s.md            ← Docker Desktop + Kubernetes setup
│   ├── aws-ecs-terraform.md    ← Production ECS architecture
│   ├── observability.md        ← Logging, metrics, tracing standards
│   └── ci-cd.md                ← GitHub Actions pipeline spec
├── adr/
│   ├── 001-java21-spring-boot.md
│   ├── 002-hexagonal-architecture.md
│   ├── 003-database-per-service.md
│   ├── 004-event-driven-communication.md
│   ├── 005-anonymous-identity.md
│   └── 006-ai-moderation-pipeline.md
└── standards/
    ├── coding-standards.md     ← Java conventions, patterns, anti-patterns
    ├── testing-strategy.md     ← Test pyramid, coverage, tooling
    └── security.md             ← OWASP, data privacy, mental health data
```
