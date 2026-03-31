package com.mentalhealthforum.forumservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ForumThread aggregate root.
 * Uses AnonymousAuthorId for privacy - the actual userId is never stored.
 */
public class ForumThread {

    private UUID id;
    private UUID categoryId;
    private String title;
    private String content;
    private AnonymousAuthorId authorId;
    private String authorDisplayName;
    private ThreadStatus status;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ForumThread() {}

    public static ForumThread create(UUID categoryId, String title, String content,
                                     AnonymousAuthorId authorId, String authorDisplayName) {
        ForumThread thread = new ForumThread();
        thread.id = UUID.randomUUID();
        thread.categoryId = categoryId;
        thread.title = title;
        thread.content = content;
        thread.authorId = authorId;
        thread.authorDisplayName = authorDisplayName;
        thread.status = ThreadStatus.OPEN;
        thread.viewCount = 0;
        thread.createdAt = LocalDateTime.now();
        thread.updatedAt = LocalDateTime.now();
        return thread;
    }

    // Reconstitution constructor (used by persistence adapter)
    public static ForumThread reconstitute(UUID id, UUID categoryId, String title, String content,
                                           AnonymousAuthorId authorId, String authorDisplayName,
                                           ThreadStatus status, int viewCount,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        ForumThread thread = new ForumThread();
        thread.id = id;
        thread.categoryId = categoryId;
        thread.title = title;
        thread.content = content;
        thread.authorId = authorId;
        thread.authorDisplayName = authorDisplayName;
        thread.status = status;
        thread.viewCount = viewCount;
        thread.createdAt = createdAt;
        thread.updatedAt = updatedAt;
        return thread;
    }

    public void incrementViewCount() {
        this.viewCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateContent(String title, String content) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void lock() {
        this.status = ThreadStatus.LOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    public void pin() {
        this.status = ThreadStatus.PINNED;
        this.updatedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = ThreadStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public AnonymousAuthorId getAuthorId() { return authorId; }
    public String getAuthorDisplayName() { return authorDisplayName; }
    public ThreadStatus getStatus() { return status; }
    public int getViewCount() { return viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
