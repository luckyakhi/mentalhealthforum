package com.mentalhealthforum.discussion.domain.repository;

import com.mentalhealthforum.discussion.domain.model.ForumThread;
import com.mentalhealthforum.discussion.domain.model.ThreadId;

import java.util.Optional;

/**
 * Domain Repository Interface for ForumThread.
 * Defines the contract for persistence without binding to specific technology.
 */
public interface ThreadRepository {
    void save(ForumThread thread);

    Optional<ForumThread> findById(ThreadId id);
}
