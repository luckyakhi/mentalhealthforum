package com.mentalhealthforum.forumservice.domain.repository;

import com.mentalhealthforum.forumservice.domain.model.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) for ForumThread persistence.
 */
public interface ThreadRepository {

    ForumThread save(ForumThread thread);

    Optional<ForumThread> findById(UUID id);

    Page<ForumThread> findAll(Pageable pageable);

    Page<ForumThread> findByCategoryId(UUID categoryId, Pageable pageable);

    Page<ForumThread> search(String keyword, Pageable pageable);

    long countByCategoryId(UUID categoryId);

    void delete(UUID id);
}
