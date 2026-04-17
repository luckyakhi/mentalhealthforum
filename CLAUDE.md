# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
A mental health platform (anxietyaicure.com) starting with an anonymous forum, evolving into AI-assisted health suggestions from human experts. Built with Java 21, Spring Boot 3.5.x, microservices architecture deployed on AWS ECS via Terraform.

## Specs-Driven Development
All implementation MUST be driven by specs in `/specs`. Before writing any code:
1. Read the relevant service spec in `/specs/services/` for the service being modified
2. Follow the API contract in `/specs/api/` (OpenAPI 3.1 YAML files) — source of truth for endpoints
3. Respect ADRs in `/specs/adr/` — binding architectural decisions
4. See `/specs/SPECS-INDEX.md` for a full map of all specs

## Build & Run Commands

### Backend services (Maven wrapper, Java 21)
```bash
cd backend/<service-name>       # user-service or forum-service
./mvnw spring-boot:run          # Run locally
./mvnw test                     # Run all tests
./mvnw test -Dtest=ClassName    # Run a single test class
./mvnw test -Dtest=ClassName#methodName  # Run a single test method
./mvnw package -DskipTests      # Build JAR without tests
```

### Frontend (React 19, Vite, TypeScript)
```bash
cd frontend
npm install         # Install dependencies
npm run dev         # Dev server
npm run build       # Production build (tsc + vite build)
npm run lint        # ESLint
```

### Docker & Kubernetes (local)
```bash
./k8s-build.sh      # Build all 3 Docker images locally
./k8s-deploy.sh     # Deploy to local k8s (Docker Desktop)
# Or use Skaffold for live-reload:
cd k8s && skaffold dev --namespace mental-health-forum
```
Local ports: Frontend :30080, User Service :30081, Forum Service :30082

### AWS Infrastructure
```bash
cd terraform && terraform plan && terraform apply   # Provision ECS/RDS/ALB
./deploy-aws.sh     # Deploy to AWS
./start-server.sh   # Scale up ECS + start RDS (cost savings)
./stop-server.sh    # Scale down ECS + stop RDS
```

## Architecture

### Hexagonal/DDD per service (see `/specs/architecture/hexagonal-architecture.md`)
Each backend service follows this package layout under `com.mentalhealthforum.<servicename>`:
- **`domain/model/`** — Aggregate roots, value objects (records). Domain logic lives HERE, never in services or controllers.
- **`domain/repository/`** — Port interfaces (repository contracts)
- **`application/dto/`** — Request/response DTOs
- **`application/service/`** — Application services (orchestration only, no business logic)
- **`infrastructure/web/`** — REST controllers (inbound adapters)
- **`infrastructure/persistence/`** — JPA entities, Spring Data repos, repository adapter implementations
- **`infrastructure/security/`** — JWT auth filter, security config

### Services
- **user-service** — Registration, login, JWT auth, user profiles. Owns `userdb` (PostgreSQL).
- **forum-service** — Categories, threads, comments, anonymous posting. Owns `forumdb` (PostgreSQL).
- **frontend** — React 19 SPA with React Router v6, Axios for API calls, AuthContext for JWT state.

### Cross-cutting
- All cross-service communication via async events (Kafka), never direct HTTP between services
- Each service owns its own database — no shared databases
- JWT tokens issued by user-service, validated independently by each service

## Code Conventions
- Java 21+ features: records, sealed interfaces, pattern matching, virtual threads
- No `null` — use `Optional<T>` for optional values
- Immutable domain objects (records or final fields)
- MapStruct for DTO mapping, Lombok only for builders/loggers
- Tests: unit tests for domain, integration tests for adapters, contract tests for APIs

## Branch Naming
- `feature/<service>-<description>` for new features
- `fix/<service>-<description>` for bug fixes
- `infra/<description>` for infrastructure changes
- `specs/<description>` for spec updates
