package com.mentalhealthforum.forumservice.domain.repository;

import com.mentalhealthforum.forumservice.domain.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) for Comment persistence.
 */
public interface CommentRepository {

    Comment save(Comment comment);

    Optional<Comment> findById(UUID id);

    Page<Comment> findByThreadId(UUID threadId, Pageable pageable);

    List<Comment> findByParentCommentId(UUID parentCommentId);

    void delete(UUID id);

    long countByThreadId(UUID threadId);
}
