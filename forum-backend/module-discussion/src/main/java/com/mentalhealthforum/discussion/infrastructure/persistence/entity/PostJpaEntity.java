package com.mentalhealthforum.discussion.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "forum_posts")
public class PostJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    // Default constructor for JPA
    public PostJpaEntity() {
    }

    public PostJpaEntity(UUID id, String content, Instant timestamp, UUID authorId) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.authorId = authorId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }
}
