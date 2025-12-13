package com.mentalhealthforum.discussion.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root representing a Forum Thread.
 * Manages the state and invariants of the thread and its posts.
 */
public class ForumThread {
    private final ThreadId id;
    private final String topic;
    private final String content;
    private final AnonymousAuthorId authorId;
    private ThreadStatus status;
    private SafetyFlag safetyFlag;
    private final List<Post> posts;
    private final Instant timestamp;

    public ForumThread(ThreadId id, String topic, String content, AnonymousAuthorId authorId, Instant timestamp) {
        this.id = Objects.requireNonNull(id, "ThreadId cannot be null");
        this.topic = Objects.requireNonNull(topic, "Topic cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.authorId = Objects.requireNonNull(authorId, "AuthorId cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");

        if (topic.isBlank()) {
            throw new IllegalArgumentException("Topic cannot be empty");
        }
        if (content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }

        this.status = ThreadStatus.ACTIVE;
        this.safetyFlag = SafetyFlag.SAFE;
        this.posts = new ArrayList<>();
    }

    // Business Logic

    /**
     * Adds a reply to the thread.
     * Invariant: Cannot add reply if thread is LOCKED or ARCHIVED.
     */
    public void addReply(String content, AnonymousAuthorId replyAuthorId) {
        if (this.status != ThreadStatus.ACTIVE) {
            throw new IllegalStateException("Cannot add reply to a thread that is " + this.status);
        }
        Post newPost = new Post(PostId.generate(), content, Instant.now(), replyAuthorId);
        this.posts.add(newPost);
    }

    /**
     * Flags the thread for moderation.
     * Transition: Sets SafetyFlag to NEEDS_MODERATION.
     */
    public void flagPotentialHarm() {
        this.safetyFlag = SafetyFlag.NEEDS_MODERATION;
    }

    /**
     * Locks the thread.
     */
    public void lock() {
        this.status = ThreadStatus.LOCKED;
    }

    /**
     * Archives the thread.
     */
    public void archive() {
        this.status = ThreadStatus.ARCHIVED;
    }

    // Accessors

    public ThreadId getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getContent() {
        return content;
    }

    public AnonymousAuthorId getAuthorId() {
        return authorId;
    }

    public ThreadStatus getStatus() {
        return status;
    }

    public SafetyFlag getSafetyFlag() {
        return safetyFlag;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public List<Post> getPosts() {
        return Collections.unmodifiableList(posts);
    }
}
