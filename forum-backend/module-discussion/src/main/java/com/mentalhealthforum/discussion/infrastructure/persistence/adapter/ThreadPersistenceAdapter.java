package com.mentalhealthforum.discussion.infrastructure.persistence.adapter;

import com.mentalhealthforum.discussion.domain.model.*;
import com.mentalhealthforum.discussion.domain.repository.ThreadRepository;
import com.mentalhealthforum.discussion.infrastructure.persistence.entity.PostJpaEntity;
import com.mentalhealthforum.discussion.infrastructure.persistence.entity.ThreadJpaEntity;
import com.mentalhealthforum.discussion.infrastructure.persistence.repository.ThreadSpringRepository;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ThreadPersistenceAdapter implements ThreadRepository {

    private final ThreadSpringRepository springRepository;

    public ThreadPersistenceAdapter(ThreadSpringRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public void save(ForumThread thread) {
        ThreadJpaEntity entity = mapToJpa(thread);
        springRepository.save(entity);
    }

    @Override
    public Optional<ForumThread> findById(ThreadId id) {
        return springRepository.findById(id.id()).map(this::mapToDomain);
    }

    // --- MAPPING LOGIC ---

    private ThreadJpaEntity mapToJpa(ForumThread domain) {
        ThreadJpaEntity entity = new ThreadJpaEntity(
                domain.getId().id(),
                domain.getTopic(),
                domain.getContent(),
                domain.getAuthorId().id(),
                domain.getStatus().name(),
                domain.getSafetyFlag().name(),
                domain.getTimestamp());

        List<PostJpaEntity> postEntities = domain.getPosts().stream()
                .map(p -> new PostJpaEntity(
                        p.getId().id(),
                        p.getContent(),
                        p.getTimestamp(),
                        p.getAuthorId().id()))
                .collect(Collectors.toList());

        entity.setPosts(postEntities);
        return entity;
    }

    private ForumThread mapToDomain(ThreadJpaEntity entity) {
        ThreadId threadId = new ThreadId(entity.getId());
        AnonymousAuthorId authorId = new AnonymousAuthorId(entity.getAuthorId());

        // Use reflection to bypass logic-heavy constructor if needed, or use the
        // constructor directly.
        // For ForumThread, the constructor enforces invariants on NEW threads, but for
        // rehydration from DB
        // we might want a factory or just use the constructor if the DB state is valid.
        // Assumption: DB state is valid.

        ForumThread thread = new ForumThread(
                threadId,
                entity.getTopic(),
                entity.getContent(),
                authorId,
                entity.getTimestamp());

        // Reflection to set internal state (Posts, Status, Flag) that might differ from
        // "New Thread" defaults.
        // Since the constructor initializes Status=ACTIVE, Flag=SAFE, Posts=Empty.
        try {
            setPrivateField(thread, "status", ThreadStatus.valueOf(entity.getStatus()));
            setPrivateField(thread, "safetyFlag", SafetyFlag.valueOf(entity.getSafetyFlag()));

            // Posts
            List<Post> posts = new ArrayList<>();
            for (PostJpaEntity pEntity : entity.getPosts()) {
                posts.add(new Post(
                        new PostId(pEntity.getId()),
                        pEntity.getContent(),
                        pEntity.getTimestamp(),
                        new AnonymousAuthorId(pEntity.getAuthorId())));
            }
            setPrivateField(thread, "posts", posts);

        } catch (Exception e) {
            throw new RuntimeException("Failed to rehydrate ForumThread domain object", e);
        }

        return thread;
    }

    private void setPrivateField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
