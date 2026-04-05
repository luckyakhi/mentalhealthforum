# Testing Strategy

## Test Pyramid

```
        ╱╲
       ╱  ╲        E2E Tests (few)
      ╱    ╲       - Critical user journeys only
     ╱──────╲      - Testcontainers full stack
    ╱        ╲
   ╱          ╲    Integration Tests (moderate)
  ╱            ╲   - @WebMvcTest, @DataJpaTest
 ╱──────────────╲  - Testcontainers for DB
╱                ╲
╱                  ╲ Unit Tests (many)
╱────────────────────╲ - Domain logic, pure functions
                       - No Spring context, no mocks for domain
```

## Test Types by Layer

### Domain Layer — Unit Tests
**Tool**: JUnit 5 + AssertJ
**No mocks needed** — domain objects are pure Java

```java
class ForumThreadTest {

    @Test
    void shouldRejectReplyOnClosedThread() {
        var thread = ForumThread.create(authorId, "Title", "Body text", categoryId);
        thread.close(authorId, "Resolved");

        assertThatThrownBy(() -> thread.addReply(replyAuthorId, "Reply body"))
            .isInstanceOf(ThreadClosedException.class)
            .hasMessageContaining("closed");
    }

    @Test
    void shouldIncrementReplyCountOnNewReply() {
        var thread = ForumThread.create(authorId, "Title", "Body text", categoryId);
        thread.addReply(replyAuthorId, "Reply body text");

        assertThat(thread.getReplyCount()).isEqualTo(1);
    }
}
```

### Application Layer — Unit Tests with Mocked Ports
**Tool**: JUnit 5 + Mockito + AssertJ

```java
@ExtendWith(MockitoExtension.class)
class ForumApplicationServiceTest {

    @Mock ThreadRepository threadRepository;
    @Mock EventPublisher eventPublisher;
    @InjectMocks ForumApplicationService service;

    @Test
    void shouldPublishEventWhenThreadCreated() {
        var request = new CreateThreadRequest(authorId, "Title", "Body text here", categoryId);

        service.createThread(request);

        verify(eventPublisher).publish(any(ThreadCreatedEvent.class));
        verify(threadRepository).save(any(ForumThread.class));
    }
}
```

### Infrastructure/Web — Integration Tests
**Tool**: @WebMvcTest + MockMvc

```java
@WebMvcTest(ForumController.class)
class ForumControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean CreateThreadUseCase createThreadUseCase;

    @Test
    void shouldReturn201WhenThreadCreated() throws Exception {
        when(createThreadUseCase.createThread(any()))
            .thenReturn(new CreateThreadResponse(threadId));

        mockMvc.perform(post("/api/v1/threads")
                .contentType(APPLICATION_JSON)
                .header("X-User-Anonymous-Id", "anon-test1")
                .content("""
                    {"categoryId": "uuid", "title": "Test Title", "body": "Test body text"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists());
    }
}
```

### Infrastructure/Persistence — Integration Tests
**Tool**: @DataJpaTest + Testcontainers

```java
@DataJpaTest
@Testcontainers
class JpaThreadRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired SpringDataThreadRepository jpaRepo;
    ThreadPersistenceMapper mapper = Mappers.getMapper(ThreadPersistenceMapper.class);

    @Test
    void shouldPersistAndRetrieveThread() {
        var adapter = new JpaThreadRepositoryAdapter(jpaRepo, mapper);
        var thread = ForumThread.create(authorId, "Title", "Body text", categoryId);

        adapter.save(thread);
        var found = adapter.findById(thread.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Title");
    }
}
```

## Coverage Requirements

| Layer | Minimum Coverage | Target |
|-------|-----------------|--------|
| Domain | 90% | 95% |
| Application | 80% | 90% |
| Infrastructure/Web | 70% | 80% |
| Infrastructure/Persistence | 70% | 80% |
| Overall | 80% | 85% |

## Test Naming Convention
```
should{ExpectedBehavior}When{Condition}
```
Examples:
- `shouldRejectReplyWhenThreadIsClosed`
- `shouldReturn404WhenThreadNotFound`
- `shouldPublishEventWhenUserRegistered`

## Test Data
- Use test fixtures/builders for creating domain objects
- Never use production data in tests
- Database tests use Testcontainers (PostgreSQL) — no H2

## CI Integration
- All tests run on every PR
- Unit tests: always run
- Integration tests: run if service code changed
- Coverage report posted as PR comment (JaCoCo)
- PR blocked if coverage drops below minimum
