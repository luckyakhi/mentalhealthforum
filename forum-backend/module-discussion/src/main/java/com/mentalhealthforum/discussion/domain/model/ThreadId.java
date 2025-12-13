package com.mentalhealthforum.discussion.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique Forum Thread ID.
 */
public record ThreadId(UUID id) {
    public ThreadId {
        Objects.requireNonNull(id, "Thread ID cannot be null");
    }

    public static ThreadId generate() {
        return new ThreadId(UUID.randomUUID());
    }
}
