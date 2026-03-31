package com.mentalhealthforum.forumservice.infrastructure.persistence;

import com.mentalhealthforum.forumservice.domain.model.AnonymousAuthorId;
import com.mentalhealthforum.forumservice.domain.model.ForumThread;
import com.mentalhealthforum.forumservice.domain.model.ThreadStatus;
import com.mentalhealthforum.forumservice.domain.repository.ThreadRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ThreadRepositoryAdapter implements ThreadRepository {

    private final ForumThreadJpaRepository jpaRepository;

    public ThreadRepositoryAdapter(ForumThreadJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ForumThread save(ForumThread thread) {
        ForumThreadJpaEntity entity = toEntity(thread);
        ForumThreadJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ForumThread> findById(UUID id) {
        return jpaRepository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public Page<ForumThread> findByCategoryId(UUID categoryId, Pageable pageable) {
        Page<ForumThreadJpaEntity> page = jpaRepository
                .findByCategoryIdOrderByCreatedAtDesc(categoryId.toString(), pageable);
        return page.map(this::toDomain);
    }

    @Override
    public Page<ForumThread> search(String keyword, Pageable pageable) {
        Page<ForumThreadJpaEntity> page = jpaRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable);
        return page.map(this::toDomain);
    }

    @Override
    public long countByCategoryId(UUID categoryId) {
        return jpaRepository.countByCategoryId(categoryId.toString());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id.toString());
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private ForumThreadJpaEntity toEntity(ForumThread thread) {
        return ForumThreadJpaEntity.builder()
                .id(thread.getId().toString())
                .categoryId(thread.getCategoryId().toString())
                .title(thread.getTitle())
                .content(thread.getContent())
                .authorId(thread.getAuthorId().value())
                .authorDisplayName(thread.getAuthorDisplayName())
                .status(thread.getStatus().name())
                .viewCount(thread.getViewCount())
                .createdAt(thread.getCreatedAt())
                .updatedAt(thread.getUpdatedAt())
                .build();
    }

    private ForumThread toDomain(ForumThreadJpaEntity entity) {
        return ForumThread.reconstitute(
                UUID.fromString(entity.getId()),
                UUID.fromString(entity.getCategoryId()),
                entity.getTitle(),
                entity.getContent(),
                new AnonymousAuthorId(entity.getAuthorId()),
                entity.getAuthorDisplayName(),
                ThreadStatus.valueOf(entity.getStatus()),
                entity.getViewCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
