package com.mentalhealthforum.discussion.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing an Anonymous Author.
 * Strictly wraps a UUID to ensure privacy (no direct email/username linkage).
 */
public record AnonymousAuthorId(UUID id) {
    public AnonymousAuthorId {
        Objects.requireNonNull(id, "Author ID cannot be null");
    }

    public static AnonymousAuthorId generate() {
        return new AnonymousAuthorId(UUID.randomUUID());
    }
}
