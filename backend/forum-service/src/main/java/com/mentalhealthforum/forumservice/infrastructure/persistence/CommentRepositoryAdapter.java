package com.mentalhealthforum.forumservice.infrastructure.persistence;

import com.mentalhealthforum.forumservice.domain.model.AnonymousAuthorId;
import com.mentalhealthforum.forumservice.domain.model.Comment;
import com.mentalhealthforum.forumservice.domain.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CommentRepositoryAdapter implements CommentRepository {

    private final CommentJpaRepository jpaRepository;

    public CommentRepositoryAdapter(CommentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Comment save(Comment comment) {
        CommentJpaEntity entity = toEntity(comment);
        CommentJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Comment> findById(UUID id) {
        return jpaRepository.findById(id.toString()).map(this::toDomain);
    }

    @Override
    public Page<Comment> findByThreadId(UUID threadId, Pageable pageable) {
        return jpaRepository.findByThreadIdOrderByCreatedAtAsc(threadId.toString(), pageable)
                .map(this::toDomain);
    }

    @Override
    public List<Comment> findByParentCommentId(UUID parentCommentId) {
        return jpaRepository.findByParentCommentIdOrderByCreatedAtAsc(parentCommentId.toString())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id.toString());
    }

    @Override
    public long countByThreadId(UUID threadId) {
        return jpaRepository.countByThreadId(threadId.toString());
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private CommentJpaEntity toEntity(Comment comment) {
        return CommentJpaEntity.builder()
                .id(comment.getId().toString())
                .threadId(comment.getThreadId().toString())
                .content(comment.getContent())
                .authorId(comment.getAuthorId().value())
                .authorDisplayName(comment.getAuthorDisplayName())
                .parentCommentId(comment.getParentCommentId() != null
                        ? comment.getParentCommentId().toString()
                        : null)
                .upvotes(comment.getUpvotes())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private Comment toDomain(CommentJpaEntity entity) {
        return Comment.reconstitute(
                UUID.fromString(entity.getId()),
                UUID.fromString(entity.getThreadId()),
                entity.getContent(),
                new AnonymousAuthorId(entity.getAuthorId()),
                entity.getAuthorDisplayName(),
                entity.getParentCommentId() != null
                        ? UUID.fromString(entity.getParentCommentId())
                        : null,
                entity.getUpvotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
