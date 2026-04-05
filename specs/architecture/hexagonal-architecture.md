# Hexagonal Architecture (Ports & Adapters) Guide

## Overview
Every microservice in this platform follows hexagonal architecture (also called ports & adapters). This ensures domain logic is isolated from infrastructure concerns.

## Package Structure (per service)

```
com.mentalhealthforum.<service-name>/
├── domain/
│   ├── model/           # Aggregate roots, entities, value objects, enums
│   ├── repository/      # Port interfaces (outbound)
│   ├── service/         # Domain services (pure business logic, no Spring annotations)
│   └── event/           # Domain events (records)
├── application/
│   ├── service/         # Use-case orchestration (application services)
│   ├── port/
│   │   ├── inbound/     # Inbound port interfaces (use cases)
│   │   └── outbound/    # Outbound port interfaces (external systems)
│   └── dto/             # Request/response DTOs
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/      # JPA entities (@Entity)
│   │   ├── repository/  # Spring Data JPA repositories
│   │   ├── adapter/     # Repository adapter (implements domain port)
│   │   └── mapper/      # MapStruct mappers (JPA entity <-> domain model)
│   ├── web/
│   │   ├── controller/  # REST controllers
│   │   ├── mapper/      # MapStruct mappers (DTO <-> domain model)
│   │   └── filter/      # Servlet filters, interceptors
│   ├── messaging/
│   │   ├── publisher/   # Kafka event publishers
│   │   └── consumer/    # Kafka event consumers
│   ├── external/        # External API clients (AI, email, etc.)
│   └── config/          # Spring @Configuration classes
└── <ServiceName>Application.java  # Spring Boot main class
```

## Layer Rules

### Domain Layer (`domain/`)
- **ZERO framework dependencies** — no Spring, no JPA, no Lombok (except `@Value` for immutability)
- Contains: aggregate roots, value objects, domain events, repository port interfaces
- All business rules and invariants enforced HERE
- Domain objects are **immutable** — use Java records or final fields
- Domain services contain logic that doesn't belong to a single aggregate

```java
// GOOD: Domain model as Java record
public record ThreadId(UUID value) {
    public ThreadId {
        Objects.requireNonNull(value, "ThreadId cannot be null");
    }
    public static ThreadId generate() {
        return new ThreadId(UUID.randomUUID());
    }
}

// GOOD: Aggregate root with business rules
public final class ForumThread {
    private final ThreadId id;
    private final AnonymousAuthorId authorId;
    private final String title;
    private final String body;
    private final CategoryId categoryId;
    private ThreadStatus status;
    private final List<Reply> replies;
    private final Instant createdAt;

    // Factory method — the ONLY way to create a thread
    public static ForumThread create(AnonymousAuthorId authorId, String title, String body, CategoryId categoryId) {
        validateTitle(title);
        validateBody(body);
        return new ForumThread(ThreadId.generate(), authorId, title, body, categoryId,
                              ThreadStatus.ACTIVE, new ArrayList<>(), Instant.now());
    }

    // Business rule: only active threads accept replies
    public Reply addReply(AnonymousAuthorId replyAuthorId, String replyBody) {
        if (this.status != ThreadStatus.ACTIVE) {
            throw new ThreadClosedException(this.id);
        }
        Reply reply = Reply.create(replyAuthorId, replyBody, this.id);
        this.replies.add(reply);
        return reply;
    }
}
```

### Application Layer (`application/`)
- **Orchestrates use cases** — thin layer that coordinates domain objects and ports
- Application services are annotated with `@Service` and `@Transactional`
- DTOs live here — they are the API contract between web and application layers
- NEVER contains business logic — delegates everything to domain

```java
// GOOD: Thin application service
@Service
@Transactional
public class ForumApplicationService implements CreateThreadUseCase {

    private final ThreadRepository threadRepository;    // domain port
    private final EventPublisher eventPublisher;        // outbound port

    @Override
    public CreateThreadResponse createThread(CreateThreadRequest request) {
        var thread = ForumThread.create(
            request.authorId(),
            request.title(),
            request.body(),
            request.categoryId()
        );
        threadRepository.save(thread);
        eventPublisher.publish(new ThreadCreatedEvent(thread.getId(), thread.getCategoryId()));
        return CreateThreadResponse.from(thread);
    }
}
```

### Infrastructure Layer (`infrastructure/`)
- **Implements ports** — adapters for database, messaging, web, external APIs
- JPA entities are separate from domain models — MapStruct maps between them
- Controllers are thin — validate input, delegate to application service, return response
- All Spring/framework annotations live HERE (except `@Service`/`@Transactional` in application layer)

```java
// GOOD: JPA entity (separate from domain model)
@Entity
@Table(name = "forum_threads")
public class ForumThreadEntity {
    @Id
    private UUID id;
    private UUID authorId;
    private String title;
    // ...
}

// GOOD: Repository adapter implements domain port
@Component
public class JpaThreadRepositoryAdapter implements ThreadRepository {
    private final SpringDataThreadRepository jpaRepo;
    private final ThreadPersistenceMapper mapper;

    @Override
    public void save(ForumThread thread) {
        jpaRepo.save(mapper.toEntity(thread));
    }
}
```

## Dependency Rule
Dependencies MUST point inward:

```
Infrastructure → Application → Domain
     ↓               ↓            ↓
  (adapters)    (use cases)   (pure logic)
```

- Domain depends on NOTHING
- Application depends on Domain
- Infrastructure depends on Application and Domain
- **NEVER** let Domain depend on Infrastructure

## Testing Strategy by Layer

| Layer | Test Type | Tools | What to Test |
|-------|-----------|-------|--------------|
| Domain | Unit tests | JUnit 5, AssertJ | Business rules, invariants, edge cases |
| Application | Unit tests (mocked ports) | JUnit 5, Mockito | Use-case orchestration, event publishing |
| Infrastructure/Web | Integration tests | @WebMvcTest, MockMvc | HTTP status codes, serialization, validation |
| Infrastructure/Persistence | Integration tests | @DataJpaTest, Testcontainers | Queries, mappings, constraints |
| Cross-cutting | Contract tests | Spring Cloud Contract | API contracts between services |
| End-to-end | E2E tests | Testcontainers (full stack) | Critical user journeys |
