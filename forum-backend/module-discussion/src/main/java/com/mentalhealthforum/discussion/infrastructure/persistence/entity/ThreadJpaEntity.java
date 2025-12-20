package com.mentalhealthforum.discussion.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "forum_threads")
public class ThreadJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "safety_flag", nullable = false)
    private String safetyFlag;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "thread_id")
    private List<PostJpaEntity> posts = new ArrayList<>();

    // Default constructor for JPA
    public ThreadJpaEntity() {
    }

    public ThreadJpaEntity(UUID id, String topic, String content, UUID authorId, String status, String safetyFlag,
            Instant timestamp) {
        this.id = id;
        this.topic = topic;
        this.content = content;
        this.authorId = authorId;
        this.status = status;
        this.safetyFlag = safetyFlag;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSafetyFlag() {
        return safetyFlag;
    }

    public void setSafetyFlag(String safetyFlag) {
        this.safetyFlag = safetyFlag;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<PostJpaEntity> getPosts() {
        return posts;
    }

    public void setPosts(List<PostJpaEntity> posts) {
        this.posts = posts;
    }
}
