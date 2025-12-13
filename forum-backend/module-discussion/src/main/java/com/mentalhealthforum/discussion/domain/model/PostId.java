package com.mentalhealthforum.discussion.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique Post ID.
 */
public record PostId(UUID id) {
    public PostId {
        Objects.requireNonNull(id, "Post ID cannot be null");
    }

    public static PostId generate() {
        return new PostId(UUID.randomUUID());
    }
}
