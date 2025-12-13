package com.mentalhealthforum.discussion.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a Post (reply) within a thread.
 */
public class Post {
    private final PostId id;
    private final String content;
    private final Instant timestamp;
    private final AnonymousAuthorId authorId;

    public Post(PostId id, String content, Instant timestamp, AnonymousAuthorId authorId) {
        this.id = Objects.requireNonNull(id, "PostId cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        this.authorId = Objects.requireNonNull(authorId, "AuthorId cannot be null");

        if (content.isBlank()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }
    }

    public PostId getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public AnonymousAuthorId getAuthorId() {
        return authorId;
    }
}
