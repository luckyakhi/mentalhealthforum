package com.mentalhealthforum.forumservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID threadId,
        String content,
        String authorDisplayName,
        String authorId,
        UUID parentCommentId,
        int upvotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
