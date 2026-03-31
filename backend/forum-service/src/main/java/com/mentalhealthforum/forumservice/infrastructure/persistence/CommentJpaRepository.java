package com.mentalhealthforum.forumservice.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, String> {

    Page<CommentJpaEntity> findByThreadIdOrderByCreatedAtAsc(String threadId, Pageable pageable);

    List<CommentJpaEntity> findByParentCommentIdOrderByCreatedAtAsc(String parentCommentId);

    long countByThreadId(String threadId);
}
