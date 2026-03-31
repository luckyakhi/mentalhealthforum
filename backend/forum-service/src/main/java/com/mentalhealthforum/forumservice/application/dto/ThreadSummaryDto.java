package com.mentalhealthforum.forumservice.application.dto;

import com.mentalhealthforum.forumservice.domain.model.ThreadStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ThreadSummaryDto(
        UUID id,
        UUID categoryId,
        String title,
        String authorDisplayName,
        ThreadStatus status,
        int viewCount,
        LocalDateTime createdAt,
        long commentCount
) {}
