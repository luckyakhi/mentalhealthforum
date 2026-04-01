package com.mentalhealthforum.forumservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateThreadRequest(
        UUID categoryId,
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
        String title,
        @NotBlank(message = "Content is required")
        @Size(min = 10, max = 10000, message = "Content must be between 10 and 10000 characters")
        String content
) {}
