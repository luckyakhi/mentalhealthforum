# Mental Health Forum Platform

## Project Overview
A mental health platform starting with an anonymous forum, evolving into AI-assisted health suggestions from human experts. Built with Java 21, Spring Boot 3.5.x, microservices architecture.

## Specs-Driven Development
All implementation MUST be driven by specs in the `/specs` directory. Before writing any code:
1. Read the relevant spec in `/specs/services/` for the service you're modifying
2. Follow the API contract in `/specs/api/` — these are the source of truth for endpoints
3. Respect ADRs in `/specs/adr/` — they document binding architectural decisions
4. Follow coding standards in `/specs/standards/`

## Architecture Rules
- **Hexagonal/DDD architecture** per `/specs/architecture/hexagonal-architecture.md`
- Domain logic lives in aggregate roots, NEVER in services or controllers
- All cross-service communication via async events (Kafka), never direct HTTP between services
- Each microservice owns its own database schema — no shared databases
- Port interfaces in domain layer, adapters in infrastructure layer

## Code Conventions
- Java 21+ features: records, sealed interfaces, pattern matching, virtual threads
- No `null` — use `Optional<T>` for optional values
- Immutable domain objects (records or final fields)
- MapStruct for DTO mapping, Lombok only for builders/loggers
- Tests: unit tests for domain, integration tests for adapters, contract tests for APIs

## Build & Run
```bash
# Local dev (single service)
cd backend/<service-name>
./mvnw spring-boot:run

# Docker build
docker build -t mental-health-forum/<service-name>:latest .

# Local k8s (Docker Desktop)
kubectl apply -k infrastructure/k8s/overlays/local/

# Terraform (AWS ECS)
cd infrastructure/terraform/environments/prod
terraform plan
terraform apply
```

## Branch Naming
- `feature/<service>-<description>` for new features
- `fix/<service>-<description>` for bug fixes
- `infra/<description>` for infrastructure changes
- `specs/<description>` for spec updates
