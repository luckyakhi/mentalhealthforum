# Java Coding Standards

## Language Version
Java 21+ — use modern features aggressively.

## Modern Java Features (Required)

### Records for Value Objects and DTOs
```java
// GOOD
public record ThreadId(UUID value) {
    public ThreadId {
        Objects.requireNonNull(value);
    }
}

// BAD — don't use classes for immutable data carriers
public class ThreadId {
    private final UUID value;
    // constructor, getter, equals, hashCode, toString... NO
}
```

### Sealed Interfaces for Domain Types
```java
// GOOD
public sealed interface ModerationResult
    permits Safe, NeedsReview, Crisis, Toxic {

    record Safe() implements ModerationResult {}
    record NeedsReview(String reason, BigDecimal confidence) implements ModerationResult {}
    record Crisis(String reason, BigDecimal confidence) implements ModerationResult {}
    record Toxic(String reason, BigDecimal confidence) implements ModerationResult {}
}
```

### Pattern Matching
```java
// GOOD
return switch (result) {
    case Safe() -> handleSafe();
    case Crisis(var reason, var conf) when conf.compareTo(THRESHOLD) > 0 -> handleCrisis(reason);
    case NeedsReview(var reason, _) -> enqueueReview(reason);
    case Toxic(var reason, _) -> handleToxic(reason);
    case Crisis(var reason, _) -> enqueueReview(reason); // low confidence crisis
};
```

### Virtual Threads
```java
// GOOD — use virtual threads for I/O-bound work
@Bean
public TomcatProtocolHandlerCustomizer<?> virtualThreadCustomizer() {
    return handler -> handler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
}
```

### Optional for Nullable Returns
```java
// GOOD
public Optional<ForumThread> findById(ThreadId id);

// BAD
public ForumThread findById(ThreadId id); // returns null? throws?
```

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Package | lowercase, dot-separated | `com.mentalhealthforum.userservice` |
| Class | PascalCase, noun | `ForumThread`, `ThreadRepository` |
| Interface | PascalCase, no prefix | `ThreadRepository` (not `IThreadRepository`) |
| Method | camelCase, verb | `createThread()`, `findById()` |
| Constant | UPPER_SNAKE | `MAX_TITLE_LENGTH` |
| Variable | camelCase | `threadCount` |
| Record field | camelCase | `record ThreadId(UUID value)` |

## Package Rules
- One public class per file
- Package-private classes for internal implementation
- No circular dependencies between packages
- Domain package has ZERO imports from infrastructure or Spring

## Dependency Injection
```java
// GOOD — constructor injection (final fields)
@Service
public class ForumApplicationService {
    private final ThreadRepository threadRepository;
    private final EventPublisher eventPublisher;

    public ForumApplicationService(ThreadRepository threadRepository, EventPublisher eventPublisher) {
        this.threadRepository = threadRepository;
        this.eventPublisher = eventPublisher;
    }
}

// BAD — field injection
@Autowired
private ThreadRepository threadRepository;
```

## Error Handling
```java
// Domain exceptions — in domain layer, no Spring dependency
public class ThreadClosedException extends RuntimeException {
    private final ThreadId threadId;
    public ThreadClosedException(ThreadId threadId) {
        super("Thread %s is closed".formatted(threadId.value()));
        this.threadId = threadId;
    }
}

// Global exception handler — in infrastructure/web
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ThreadClosedException.class)
    public ResponseEntity<ErrorResponse> handle(ThreadClosedException ex) {
        return ResponseEntity.status(422).body(ErrorResponse.businessRule(ex.getMessage()));
    }
}
```

## Annotations

### Allowed in Domain Layer
None. Zero framework annotations.

### Allowed in Application Layer
- `@Service`, `@Transactional`

### Allowed in Infrastructure Layer
- All Spring annotations (`@Component`, `@Repository`, `@RestController`, `@Configuration`, etc.)
- JPA annotations (`@Entity`, `@Table`, `@Column`, etc.)
- Lombok: only `@Slf4j`, `@Builder`, `@RequiredArgsConstructor`
- MapStruct: `@Mapper`, `@Mapping`

## Anti-Patterns to Avoid
- **Anemic domain model**: Don't put business logic in services — put it in aggregate roots
- **God service**: If a service class >200 lines, it's doing too much
- **Primitive obsession**: Wrap primitives in value objects (`ThreadId`, not `UUID`)
- **Feature envy**: If a method mostly accesses another object's data, move it there
- **Null returns**: Use `Optional<T>` — never return null from a public method
- **Checked exceptions**: Use unchecked (runtime) exceptions for domain errors
