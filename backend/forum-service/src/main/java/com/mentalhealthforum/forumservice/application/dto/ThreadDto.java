package com.mentalhealthforum.forumservice.application.dto;

import com.mentalhealthforum.forumservice.domain.model.ThreadStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ThreadDto(
        UUID id,
        UUID categoryId,
        String title,
        String content,
        String authorDisplayName,
        String authorId,
        ThreadStatus status,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long commentCount
) {}
