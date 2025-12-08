# Mental Health Forum - Backend Architecture

## Directory Structure

```
forum-backend/
├── pom.xml                                   # Root Parent POM (Versions, Plugins)
├── docker-compose.yml                        # Infrastructure (Isolated DBs)
│
├── forum-core-domain/                        # [Clean Architecture Core]
│   ├── pom.xml                               # Pure Java Dependencies only
│   └── src/main/java/...                     # Value Objects, Domain Events, Enums
│
├── forum-infra-shared/                       # [Cross-Cutting Concerns]
│   ├── pom.xml                               # Spring Boot Starters (Web, Security)
│   └── src/main/java/...                     # Global Error Handling, Security Config
│
├── module-identity/                          # [Bounded Context: IAM]
│   ├── pom.xml
│   └── src/main/java/...                     # User, Auth, Anonymization
│
├── module-discussion/                        # [Bounded Context: Community]
│   ├── pom.xml
│   └── src/main/java/...                     # Threads, Posts (No direct PII dependency)
│
├── module-moderation/                        # [Bounded Context: Safety]
│   ├── pom.xml
│   └── src/main/java/...                     # AI Filter, Reports
│
├── module-crisis-intervention/               # [Bounded Context: Crisis]
│   ├── pom.xml
│   └── src/main/java/...                     # Real-time Triggers
│
└── forum-api-gateway/                        # [Entry Point]
    ├── pom.xml                               # Spring Cloud Gateway
    └── src/main/java/...                     # Routing, Aggregation
```

## Dependency Rules & Decoupling

### 1. The "Discussion" vs "Identity" Barrier
*   **Rule:** `module-discussion` must **NOT** have a compilation dependency on `module-identity`.
*   **Enforcement:** Maven module dependency graph prevents this. `module-discussion` does not list `module-identity` in its `pom.xml`.
*   **Integration:**
    *   **Data Level:** Updates (e.g., User changes avatar) are propagated via **Domain Events** (Eventual Consistency).
    *   **Runtime:** The `module-discussion` schema only stores `authorAliasId` (UUID), never the email or real name.
    *   **Querying:** The UI or Gateway composes the view by calling both services if needed (Aggregation), or the forum module caches a read-model of minimal user info.

### 2. Core Domain Purity
*   `forum-core-domain` contains **NO Spring dependencies**. It holds the "Ubiquitous Language" (e.g., `SessionStatus`, `CrisisLevel`).
*   This ensures domain logic is not coupled to the framework.

### 3. Infrastructure Isolation
*   `postgres-identity` (Port 5432) stores PII.
*   `postgres-forum` (Port 5433) stores public threads.
*   Physical separation reduces the risk of accidental PII leakage into forum dumps.
