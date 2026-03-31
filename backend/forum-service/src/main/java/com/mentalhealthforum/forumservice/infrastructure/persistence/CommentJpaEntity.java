package com.mentalhealthforum.forumservice.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "thread_id", nullable = false, length = 36)
    private String threadId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false, length = 50)
    private String authorId;

    @Column(name = "author_display_name", nullable = false, length = 100)
    private String authorDisplayName;

    @Column(name = "parent_comment_id", length = 36)
    private String parentCommentId;

    @Column(name = "upvotes", nullable = false)
    private int upvotes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
