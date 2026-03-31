package com.mentalhealthforum.forumservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Comment entity - belongs to a ForumThread.
 * Uses AnonymousAuthorId for privacy.
 */
public class Comment {

    private UUID id;
    private UUID threadId;
    private String content;
    private AnonymousAuthorId authorId;
    private String authorDisplayName;
    private UUID parentCommentId; // nullable, for nested comments
    private int upvotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Comment() {}

    public static Comment create(UUID threadId, String content, AnonymousAuthorId authorId,
                                 String authorDisplayName, UUID parentCommentId) {
        Comment comment = new Comment();
        comment.id = UUID.randomUUID();
        comment.threadId = threadId;
        comment.content = content;
        comment.authorId = authorId;
        comment.authorDisplayName = authorDisplayName;
        comment.parentCommentId = parentCommentId;
        comment.upvotes = 0;
        comment.createdAt = LocalDateTime.now();
        comment.updatedAt = LocalDateTime.now();
        return comment;
    }

    // Reconstitution constructor (used by persistence adapter)
    public static Comment reconstitute(UUID id, UUID threadId, String content,
                                       AnonymousAuthorId authorId, String authorDisplayName,
                                       UUID parentCommentId, int upvotes,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        Comment comment = new Comment();
        comment.id = id;
        comment.threadId = threadId;
        comment.content = content;
        comment.authorId = authorId;
        comment.authorDisplayName = authorDisplayName;
        comment.parentCommentId = parentCommentId;
        comment.upvotes = upvotes;
        comment.createdAt = createdAt;
        comment.updatedAt = updatedAt;
        return comment;
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void upvote() {
        this.upvotes++;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getThreadId() { return threadId; }
    public String getContent() { return content; }
    public AnonymousAuthorId getAuthorId() { return authorId; }
    public String getAuthorDisplayName() { return authorDisplayName; }
    public UUID getParentCommentId() { return parentCommentId; }
    public int getUpvotes() { return upvotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
