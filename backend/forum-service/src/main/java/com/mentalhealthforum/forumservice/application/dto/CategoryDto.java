package com.mentalhealthforum.forumservice.application.dto;

import java.util.UUID;

public record CategoryDto(
        UUID id,
        String name,
        String description,
        int displayOrder,
        int threadCount
) {}
