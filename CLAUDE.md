# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
A mental health platform (anxietyaicure.com) starting with an anonymous forum, evolving into AI-assisted health suggestions from human experts. Built with Java 21, Spring Boot 3.5.x, microservices architecture deployed on AWS ECS via Terraform. Local dev runs on Docker Compose or Kubernetes (Docker Desktop).

## Specs-Driven Development
All implementation MUST be driven by specs in `/specs`. Before writing any code:
1. Read the relevant service spec in `/specs/services/` for the service being modified
2. Follow the API contract in `/specs/api/` (OpenAPI 3.1 YAML files) — source of truth for endpoints
3. Respect ADRs in `/specs/adr/` — binding architectural decisions (Java 21, hexagonal, DB-per-service, event-driven, anonymous identity, AI moderation)
4. See `/specs/SPECS-INDEX.md` for a full map of all specs

## Build & Run Commands

### Backend services (Maven wrapper, Java 21)
```bash
cd backend/<service-name>       # user-service or forum-service
./mvnw spring-boot:run          # Run locally (needs user-db/forum-db reachable)
./mvnw test                     # Run all tests (uses H2 in-memory DB)
./mvnw test -Dtest=ClassName    # Run a single test class
./mvnw test -Dtest=ClassName#methodName  # Run a single test method
./mvnw package -DskipTests      # Build JAR without tests
```
Internal ports: `user-service` listens on **8080**, `forum-service` listens on **8081** (see `application.properties`). DB/JWT config is env-driven (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`) with dev defaults.

### Frontend (React 19, Vite, TypeScript)
```bash
cd frontend
npm install         # Install dependencies
npm run dev         # Dev server
npm run build       # Production build (tsc -b + vite build)
npm run lint        # ESLint
```
Service URLs come from `VITE_USER_SERVICE_URL` / `VITE_FORUM_SERVICE_URL` build-time env vars (see `src/api/config.ts`).

### Docker Compose (quickest full-stack dev loop)
```bash
docker compose up --build       # Brings up both DBs, both services, and the frontend
```
Exposes frontend :30080, user-service :30081→8080, forum-service :30082→8081, user-db :5432, forum-db :5433.

### Kubernetes (local, Docker Desktop)
```bash
./k8s-build.sh      # Build all 3 Docker images locally (imagePullPolicy: Never)
./k8s-deploy.sh     # Deploy to local k8s: namespace, secrets, DBs (StatefulSets), services
# Or live-reload:
cd k8s && skaffold dev --namespace mental-health-forum
```
Local NodePorts: Frontend :30080, User Service :30081, Forum Service :30082. See `QUICKSTART.md` for kubectl cheatsheet and teardown.

### AWS Infrastructure
```bash
cd terraform && terraform plan && terraform apply   # Provision ECS/RDS/ALB/ACM/Route53
./deploy-aws.sh     # Build + push images to ECR + force-deploy ECS services
./start-server.sh   # Scale up ECS + start RDS (cost savings)
./stop-server.sh    # Scale down ECS + stop RDS
```

## Architecture

### Hexagonal/DDD per service (see `/specs/architecture/hexagonal-architecture.md` and ADR 002)
Each backend service follows this package layout under `com.mentalhealthforum.<servicename>`:
- **`domain/model/`** — Aggregate roots + value objects as Java **records** (e.g. `User`, `UserId`, `ForumThread`, `Comment`, `Category`, `AnonymousAuthorId`). Domain logic lives HERE, never in services or controllers.
- **`domain/repository/`** — Port interfaces (repository contracts) that speak in domain records.
- **`application/service/`** — One orchestration class per service (`UserApplicationService`, `ForumApplicationService`) that all controllers delegate to. Orchestration only — no business logic.
- **`application/dto/`** — Request/response DTOs (MapStruct converts between DTOs, domain records, and JPA entities).
- **`infrastructure/web/`** — REST controllers + `GlobalExceptionHandler`. Forum-service also has `SecurityContextHelper` to pull the authenticated user from the JWT.
- **`infrastructure/persistence/`** — `*JpaEntity` classes (mutable, JPA-annotated), `*JpaRepository` (Spring Data), and `*RepositoryAdapter` classes that implement the domain port by translating between JPA entities and domain records. **Never let JPA entities leak out of `persistence/`.**
- **`infrastructure/security/`** — `JwtService`, `JwtAuthFilter`, `SecurityConfig`. Schema is `spring.jpa.hibernate.ddl-auto=update` — entities are the schema source; no migration tool is wired in yet.

### Services
- **user-service** — Registration, login, JWT issuance, user profiles. Owns `userdb` (PostgreSQL). Only this service **issues** JWTs (`jwt.expiration=86400000` = 24h).
- **forum-service** — Categories, threads, comments. Owns `forumdb` (PostgreSQL). Only **validates** JWTs — never calls back to user-service.
- **frontend** — React 19 SPA, React Router v6. Two separate Axios instances in `src/api/config.ts` (`userServiceApi`, `forumServiceApi`); both request-interceptors attach `Authorization: Bearer <token>` from `localStorage`. JWT state is held in `src/context/AuthContext.tsx`; `ProtectedRoute` guards private routes.

### Cross-cutting invariants
- **Shared JWT secret, independent validation.** Both services read the same `JWT_SECRET` env var (HMAC-SHA). user-service signs, forum-service verifies locally. Keep the secret in sync across services.
- **Database-per-service** (ADR 003) — no shared tables, no cross-service JDBC.
- **Event-driven communication via Kafka** is the target (ADR 004 / `specs/architecture/event-driven.md`) but is **not yet wired up** — no Kafka dependencies in either `pom.xml`. Today services stay decoupled only through DB-per-service and independent JWT validation. If you add cross-service logic, add the Kafka infrastructure rather than introducing synchronous HTTP between services.
- **Anonymous identity** (ADR 005) — forum-service never stores the real `userId` on posts. `AnonymousAuthorId.from(userId, salt)` takes SHA-256 of `userId+salt` and returns `anon-<8-hex>`. Preserve this derivation whenever a new post/comment is created.

## Code Conventions
- Java 21+ features: records for domain + DTOs, sealed interfaces, pattern matching, virtual threads where useful.
- No `null` in domain code — use `Optional<T>` for optional values.
- Immutable domain objects (records or `final` fields only).
- **MapStruct** for DTO/entity/record mapping; **Lombok** only for builders and loggers (excluded from the final jar — see `spring-boot-maven-plugin` config).
- Tests: unit tests for domain, integration tests for adapters (H2), contract tests for APIs against the OpenAPI specs in `/specs/api/`.

## Branch Naming
- `feature/<service>-<description>` for new features
- `fix/<service>-<description>` for bug fixes
- `infra/<description>` for infrastructure changes
- `specs/<description>` for spec updates
